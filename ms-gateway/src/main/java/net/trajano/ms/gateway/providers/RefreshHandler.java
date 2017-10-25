package net.trajano.ms.gateway.providers;

import static io.vertx.core.http.HttpHeaders.DATE;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static net.trajano.ms.gateway.providers.RequestIDProvider.REQUEST_ID;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Conversions;
import net.trajano.ms.gateway.internal.MediaTypes;

/**
 * This handler deals with refreshing the OAuth token.
 */
@Component
public class RefreshHandler implements
    Handler<RoutingContext> {

    /**
     * Bad request status message.
     */
    private static final String BAD_REQUEST = "Bad Request";

    private static final String ERROR = "error";

    private static final String ERROR_DESCRIPTION = "error_description";

    /**
     * This is the pattern for the refresh token used.
     */
    private static final String TOKEN_PATTERN = "^[A-Za-z0-9]{64}$";

    @Value("${authorization.endpoint}")
    private URI authorizationEndpoint;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private RequestIDProvider requestIDProvider;

    @Override
    public void handle(final RoutingContext context) {

        final HttpServerRequest contextRequest = context.request();
        final HttpServerResponse contextResponse = context.response();

        final String requestID = requestIDProvider.newRequestID(context);

        final String grantType = contextRequest.getFormAttribute("grant_type");
        if (grantType == null) {
            contextResponse
                .setChunked(false)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                .setStatusCode(400)
                .setStatusMessage(BAD_REQUEST)
                .end(new JsonObject()
                    .put(ERROR, "invalid_grant")
                    .put(ERROR_DESCRIPTION, "Missing grant type")
                    .toBuffer());
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
                .end(new JsonObject()
                    .put(ERROR, "invalid_grant")
                    .put(ERROR_DESCRIPTION, "Missing authorization")
                    .toBuffer());
            return;
        }

        if (!"refresh_token".equals(grantType)) {
            contextResponse
                .setChunked(false)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                .setStatusCode(400)
                .setStatusMessage(BAD_REQUEST)
                .end(new JsonObject()
                    .put(ERROR, "unsupported_grant_type")
                    .put(ERROR_DESCRIPTION, "Unsupported grant type")
                    .toBuffer());
            return;
        }
        final String refreshToken = contextRequest.getFormAttribute("refresh_token");
        if (refreshToken == null || !refreshToken.matches(TOKEN_PATTERN)) {
            contextResponse
                .setChunked(false)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                .setStatusCode(400)
                .setStatusMessage(BAD_REQUEST)
                .end(new JsonObject()
                    .put(ERROR, "invalid_request")
                    .put(ERROR_DESCRIPTION, "Missing grant")
                    .toBuffer());
            return;
        }

        // Trust the authorization endpoint and use the body handler
        final HttpClientRequest authorizationRequest = httpClient.post(Conversions.toRequestOptions(authorizationEndpoint),
            authorizationResponse -> authorizationResponse.bodyHandler(contextResponse
                .setChunked(false)
                .setStatusCode(authorizationResponse.statusCode())
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                .putHeader(RequestIDProvider.REQUEST_ID, requestID)
                .setStatusMessage(authorizationResponse.statusMessage())::end));
        authorizationRequest
            .putHeader(HttpHeaders.AUTHORIZATION, authorization)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
            .putHeader(HttpHeaders.ACCEPT, "application/json")
            .putHeader(REQUEST_ID, requestID)
            .putHeader(DATE, RFC_1123_DATE_TIME.format(now(UTC)))
            .end("grant_type=refresh_token&refresh_token=" + refreshToken);

    }
}
