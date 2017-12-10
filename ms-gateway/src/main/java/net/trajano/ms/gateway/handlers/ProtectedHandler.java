package net.trajano.ms.gateway.handlers;

import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.DATE;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_FORM_URLENCODED;
import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_JSON;
import static net.trajano.ms.gateway.providers.RequestIDProvider.REQUEST_ID;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Conversions;
import net.trajano.ms.gateway.internal.Errors;
import net.trajano.ms.gateway.internal.PathContext;
import net.trajano.ms.gateway.providers.GatewayClientAuthorization;
import net.trajano.ms.gateway.providers.RequestIDProvider;

@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_PATHS + 5)
public class ProtectedHandler extends SelfRegisteringRoutingContextHandler {

    private static final String BEARER_TOKEN_PATTERN = "^Bearer ([A-Za-z0-9]{64})$";

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    private static final Logger LOG = LoggerFactory.getLogger(ProtectedHandler.class);

    private static final String X_JWKS_URI = "X-JWKS-URI";

    private static final String X_JWT_ASSERTION = "X-JWT-Assertion";

    private static final String X_JWT_AUDIENCE = "X-JWT-Audience";

    @Value("${authorization.token_endpoint}")
    private URI authorizationEndpoint;

    @Autowired
    private GatewayClientAuthorization gatewayClientAuthorization;

    @Autowired
    private HttpClient httpClient;

    @Value("${jwks.path}")
    private String jwksPath;

    @Value("${jwks.uri}")
    private URI jwksUri;

    /**
     * Obtains the access token from the request. It is expected to be the
     * Authorization with a bearer tag. The authentication code is expected to be a
     * given pattern.
     *
     * @param contextRequest
     *            request
     * @return access token
     */
    private String getAccessToken(final HttpServerRequest contextRequest) {

        final String authorizationHeader = contextRequest.getHeader(AUTHORIZATION);
        if (authorizationHeader == null) {
            return null;
        }
        final Matcher m = Pattern.compile(BEARER_TOKEN_PATTERN).matcher(authorizationHeader);
        if (!m.matches()) {
            return null;
        } else {
            return m.group(1);
        }
    }

    /**
     * This handler goes through the authorization to prepopulate the
     * X-JWT-Assertion header. There is no need to check if client is authorized
     * because it was already done by ClientOriginHandler.
     *
     * @return handler
     */
    @Override
    @SuppressWarnings("unchecked")
    public void handle(final RoutingContext context) {

        LOG.debug("Handling {} ended={}", context, context.request().isEnded());
        // if null or it is marked as protected then go on.
        final PathContext pathContext = PathContext.get(context);
        if (pathContext == null || !pathContext.isProtected()) {
            context.next();
            return;
        }
        LOG.debug("Handling {} with from={} to={} protected={}", context, pathContext.getFrom(), pathContext.getTo(), pathContext.isProtected());

        final String baseUri = pathContext.getFrom();

        final HttpServerRequest contextRequest = context.request();
        final HttpServerResponse contextResponse = context.response();

        if (!contextRequest.uri().startsWith(baseUri)) {
            throw new IllegalStateException(contextRequest.uri() + " did not start with" + baseUri);
        }

        final String accessToken = getAccessToken(contextRequest);

        final String requestID = context.get(RequestIDProvider.REQUEST_ID);
        final String now = RFC_1123_DATE_TIME.format(now(UTC));

        if (accessToken == null) {
            LOG.debug("missing or invalid access token");
            contextResponse
                .setStatusCode(401)
                .setStatusMessage("Unauthorized")
                .putHeader("WWW-Authenticate", "Bearer")
                .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .end(Errors.unauthorizedClient("missing or invalid access token").toBuffer());
            return;
        }

        final HttpClientRequest authorizationRequest = httpClient
            .post(Conversions.toRequestOptions(authorizationEndpoint))
            .handler(authorizationResponse -> {
                LOG.debug("ended={}", contextRequest.isEnded());
                authorizationResponse.bodyHandler(buffer -> {
                    LOG.debug("isended={}", contextRequest.isEnded());
                    if (authorizationResponse.statusCode() != 200) {
                        authorizationResponse.headers().forEach(h -> stripTransferEncodingAndLength(contextResponse, h));
                        contextResponse.setStatusCode(authorizationResponse.statusCode())
                            .setStatusMessage(authorizationResponse.statusMessage())
                            .setChunked(false)
                            .end(buffer);
                    } else {
                        final JsonObject response = new JsonObject(buffer);
                        final String idToken = response.getString("id_token");

                        final List<String> audience = response.getJsonArray("aud").getList();
                        if (idToken == null) {
                            LOG.error("Unable to get the ID Token from {} given access_token={}", authorizationEndpoint, accessToken);
                            context.response().setStatusCode(500)
                                .setStatusMessage(INTERNAL_SERVER_ERROR)
                                .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .end(Errors.serverError("Unable to get assertion from authorization endpoint").toBuffer());
                            return;
                        }

                        final Map<String, String> securityMap = new HashMap<>();
                        securityMap.put(X_JWT_ASSERTION, idToken);
                        securityMap.put(X_JWKS_URI, jwksUri.toASCIIString());
                        securityMap.put(X_JWT_AUDIENCE, audience.stream().collect(Collectors.joining(", ")));
                        context.put("additional_headers", securityMap);
                        contextRequest.resume();
                        context.next();
                    }
                });

            })
            .exceptionHandler(context::fail);

        gatewayClientAuthorization.gatewayRequest(authorizationRequest)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_FORM_URLENCODED)
            .putHeader(HttpHeaders.ACCEPT, APPLICATION_JSON)
            .putHeader(REQUEST_ID, requestID)
            .putHeader(DATE, now)
            .end("grant_type=authorization_code&code=" + accessToken);
        LOG.debug("access_token={} ended={}", accessToken, contextRequest.isEnded());
    }

    @Override
    public void register(final Router router) {

        router.get().handler(this);
        router.post().handler(this);
        router.put().handler(this);
        router.delete().handler(this);
    }

    private void stripTransferEncodingAndLength(final HttpServerResponse contextResponse,
        final Map.Entry<String, String> h) {

        if ("Content-Length".equalsIgnoreCase(h.getKey()) || "Transfer-Encoding".equalsIgnoreCase(h.getKey())) {
            return;
        }
        contextResponse.putHeader(h.getKey(), h.getValue());
    }

}
