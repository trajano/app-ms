package net.trajano.ms.gateway.handlers;

import static io.vertx.core.http.HttpHeaders.DATE;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_FORM_URLENCODED;
import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_JSON;
import static net.trajano.ms.gateway.providers.RequestIDProvider.REQUEST_ID;

import java.net.URI;
import java.util.regex.Pattern;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import net.trajano.ms.gateway.providers.RequestIDProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Conversions;
import net.trajano.ms.gateway.internal.Errors;
import net.trajano.ms.gateway.internal.MediaTypes;

/**
 * This handler deals with refreshing the OAuth token.
 */
@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_PATHS)
public class RevocationHandler implements
    SelfRegisteringRoutingContextHandler {

    /**
     * Bad request status message.
     */
    private static final String BAD_REQUEST = "Bad Request";

    /**
     * This is the pattern for the refresh token used.
     */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9]{64}$");

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private RequestIDProvider requestIDProvider;

    @Value("${authorization.revocation_endpoint}")
    private URI revocationEndpoint;

    @Override
    public void handle(final RoutingContext context) {

        final HttpServerRequest contextRequest = context.request();
        final HttpServerResponse contextResponse = context.response();

        final String requestID = requestIDProvider.newRequestID(context);

        final String token = contextRequest.getFormAttribute("token");
        if (token == null || !TOKEN_PATTERN.matcher(token).matches()) {
            contextResponse
                .setChunked(false)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                .setStatusCode(400)
                .setStatusMessage(BAD_REQUEST)
                .end(Errors.invalidRequest("Missing or invalid token " + token).toBuffer());
            return;
        }

        final String authorization = contextRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            contextResponse
                .setChunked(false)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                .setStatusCode(401)
                .setStatusMessage("Unauthorized Client")
                .putHeader("WWW-Authenticate", "Basic")
                .end(Errors.unauthorizedClient("Missing authorization").toBuffer());
            return;
        }

        // Trust the authorization endpoint and use the body handler
        final HttpClientRequest revocationRequest = httpClient.post(Conversions.toRequestOptions(revocationEndpoint),
            authorizationResponse -> authorizationResponse.bodyHandler(contextResponse
                .setChunked(false)
                .setStatusCode(authorizationResponse.statusCode())
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                .putHeader(RequestIDProvider.REQUEST_ID, requestID)
                .setStatusMessage(authorizationResponse.statusMessage())::end));
        revocationRequest
            .putHeader(HttpHeaders.AUTHORIZATION, authorization)
            .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_FORM_URLENCODED)
            .putHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_JSON)
            .putHeader(REQUEST_ID, requestID)
            .putHeader(DATE, RFC_1123_DATE_TIME.format(now(UTC)))
            .end("token_type_hint=refresh_token&token=" + token);

    }

    @Value("${authorization.revocation_path:/logoff}")
    private String revocationPath;

    @Override
    public void register(Router router) {

        router.post(revocationPath)
            .consumes(APPLICATION_FORM_URLENCODED)
            .produces(APPLICATION_JSON)
            .handler(BodyHandler.create().setBodyLimit(1024));
        router.post(revocationPath)
            .consumes(APPLICATION_FORM_URLENCODED)
            .produces(APPLICATION_JSON)
            .handler(this);

    }
}
