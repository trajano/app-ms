package net.trajano.ms.gateway.providers;

import java.net.ConnectException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Conversions;

@Configuration
@Component
public class Handlers {

    private static final Logger LOG = LoggerFactory.getLogger(Handlers.class);

    private static final String TOKEN_PATTERN = "^[A-Za-z0-9]{64}$";

    @Value("${authorization.endpoint}")
    private URI authorizationEndpoint;

    @Autowired
    private HttpClient httpClient;

    public Handler<RoutingContext> failureHandler() {

        return context -> {
            LOG.error("Unhandled server exception", context.failure());
            if (!context.response().ended()) {
                if (context.failure() instanceof ConnectException) {
                    context.response().setStatusCode(503)
                        .setStatusMessage("Service Unavailable")
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(new JsonObject()
                            .put("error", "server_error")
                            .put("error_description", "Service Unavailable")
                            .toBuffer());
                } else {
                    context.response().setStatusCode(500)
                        .setStatusMessage("Internal Server Error")
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(new JsonObject()
                            .put("error", "server_error")
                            .put("error_description", "Internal Server Error")
                            .toBuffer());
                }
            }
        };

    }

    /**
     * Obtains the access token from the request. Since the Authorization header
     * can have multiple values comma separated, it needs to be broken up first
     * then we have to locate the Bearer token from the comma separated list.
     * The bearer token is expected to contain the access token.
     *
     * @param contextRequest
     *            request
     * @return access token
     */
    private String getAccessToken(final HttpServerRequest contextRequest,
        final HttpServerResponse contextResponse) {

        final String authorizationHeader = contextRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null) {
            return null;
        }

        for (final String authorization : authorizationHeader.split(",")) {
            final String cleanedAuthorization = authorization.trim();
            if (cleanedAuthorization.startsWith("Bearer ")) {
                return cleanedAuthorization.substring(7);
            }
        }
        return null;
    }

    /**
     * Obtains the client credentials from the request. Since the Authorization
     * header can have multiple values comma separated, it needs to be broken up
     * first then we have to locate the Basic authorization value from the comma
     * separated list.
     *
     * @param contextRequest
     *            request
     * @return basic authorization (including "Basic")
     */
    private String getClientCredentials(final HttpServerRequest contextRequest,
        final HttpServerResponse contextResponse) {

        final String authorizationHeader = contextRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null) {
            return null;
        }

        for (final String authorization : authorizationHeader.split(",")) {
            final String cleanedAuthorization = authorization.trim();
            if (cleanedAuthorization.startsWith("Basic ")) {
                return cleanedAuthorization;
            }
        }
        return null;
    }

    /**
     * This handler goes through the authorization to prepopulate the
     * X-JWT-Assertion header.
     *
     * @return
     */
    public Handler<RoutingContext> protectedHandler(final String baseUri,
        final URI endpoint) {

        return context -> {
            final HttpServerRequest contextRequest = context.request();
            final HttpServerResponse contextResponse = context.response();

            if (!contextRequest.uri().startsWith(baseUri)) {
                throw new IllegalStateException(contextRequest.uri() + " did not start with" + baseUri);
            }

            final String accessToken = getAccessToken(contextRequest, contextResponse);

            if (accessToken == null) {
                LOG.debug("missing access token");
                contextResponse
                    .setStatusCode(401)
                    .setStatusMessage("Unauthorized")
                    .putHeader("WWW-Authenticate", "Bearer")
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(new JsonObject()
                        .put("error", "invalid_request")
                        .put("error_description", "Missing access authorization")
                        .toBuffer());
                return;
            }
            if (!accessToken.matches(TOKEN_PATTERN)) {
                LOG.debug("invalid token={}", accessToken);
                contextResponse
                    .setStatusCode(400)
                    .setStatusMessage("Bad Request")
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(new JsonObject()
                        .put("error", "invalid_request")
                        .put("error_description", "Token not valid")
                        .toBuffer());
                return;
            }

            final String clientCredentials = getClientCredentials(contextRequest, contextResponse);
            if (clientCredentials == null) {
                contextResponse
                    .setStatusCode(401)
                    .setStatusMessage("Unauthorized")
                    .putHeader("WWW-Authenticate", "Basic")
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(new JsonObject()
                        .put("error", "invalid_request")
                        .put("error_description", "Missing client authorization")
                        .toBuffer());
                return;
            }
            contextRequest.setExpectMultipart(context.parsedHeaders().contentType().isPermitted() && "multipart".equals(context.parsedHeaders().contentType().component()));
            contextRequest.pause();
            LOG.debug("access_token={} client_credentials={}", accessToken, clientCredentials);
            final RequestOptions clientRequestOptions = Conversions.toRequestOptions(endpoint, contextRequest.uri().substring(baseUri.length()));

            final HttpClientRequest authorizationRequest = httpClient.post(Conversions.toRequestOptions(authorizationEndpoint), authorizationResponse -> {
                // Trust the authorization endpoint
                authorizationResponse.bodyHandler(buffer -> {

                    if (authorizationResponse.statusCode() != 200) {
                        contextResponse.setStatusCode(authorizationResponse.statusCode());
                        contextResponse.setStatusMessage(authorizationResponse.statusMessage());
                        authorizationResponse.headers().forEach(h -> contextResponse.putHeader(h.getKey(), h.getValue()));
                        contextResponse.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                        contextResponse.end(buffer);
                    } else {
                        final String idToken = new JsonObject(buffer).getString("id_token");
                        if (idToken == null) {
                            LOG.error("Unable to get the ID Token from {} given access_token={}", authorizationEndpoint, accessToken);
                            context.response().setStatusCode(500)
                                .setStatusMessage("Internal Server Error")
                                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .end(new JsonObject()
                                    .put("error", "server_error")
                                    .put("error_description", "Unable to get assertion from authorization endpoint")
                                    .toBuffer());
                            return;
                        }

                        final HttpClientRequest clientRequest = httpClient.request(contextRequest.method(), clientRequestOptions, clientResponse -> {
                            contextResponse.setChunked(true)
                                .setStatusCode(clientResponse.statusCode());
                            contextResponse.headers().setAll(clientResponse.headers());
                            clientResponse.handler(contextResponse::write)
                                .endHandler(v -> contextResponse.end());
                        }).exceptionHandler(context::fail);

                        clientRequest.setChunked(true)
                            .headers().setAll(contextRequest.headers());
                        clientRequest.putHeader("X-JWT-Assertion", idToken);
                        contextRequest.resume();
                        contextRequest.handler(clientRequest::write)
                            .endHandler(v -> clientRequest.end())
                            .exceptionHandler(context::fail);
                    }
                }).exceptionHandler(context::fail);
            }).exceptionHandler(context::fail);

            authorizationRequest
                .putHeader(HttpHeaders.AUTHORIZATION, clientCredentials)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .putHeader(HttpHeaders.ACCEPT, "application/json")
                .end("grant_type=authorization_code&code=" + accessToken);

        };
    }

    /**
     * This handler deals with refreshing the OAuth token.
     *
     * @return handler
     */
    public Handler<RoutingContext> refreshHandler() {

        return context -> {
            final HttpServerRequest contextRequest = context.request();
            final HttpServerResponse contextResponse = context.response();

            contextRequest
                .handler(buf -> {
                })
                .endHandler(v -> {
                    final String grantType = contextRequest.getFormAttribute("grant_type");
                    if (grantType == null) {
                        contextResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .setStatusCode(400)
                            .setStatusMessage("Bad Request")
                            .end(new JsonObject()
                                .put("error", "invalid_grant")
                                .put("error_description", "Missing grant type")
                                .toBuffer());
                        return;
                    }

                    if (!"refresh_token".equals(grantType)) {
                        contextResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .setStatusCode(400)
                            .setStatusMessage("Bad Request")
                            .end(new JsonObject()
                                .put("error", "unsupported_grant_type")
                                .put("error_description", "Unsupported grant type")
                                .toBuffer());
                        return;
                    }
                    final String refreshToken = contextRequest.getFormAttribute("refresh_token");
                    if (refreshToken == null || !refreshToken.matches(TOKEN_PATTERN)) {
                        contextResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .setStatusCode(400)
                            .setStatusMessage("Bad Request")
                            .end(new JsonObject()
                                .put("error", "invalid_request")
                                .put("error_description", "Missing grant")
                                .toBuffer());
                        return;
                    }

                    final HttpClientRequest authorizationRequest = httpClient.post(Conversions.toRequestOptions(authorizationEndpoint), authorizationResponse -> {
                        // Trust the authorization endpoint
                        authorizationResponse.bodyHandler(buffer -> {
                            contextResponse.setStatusCode(authorizationResponse.statusCode());
                            contextResponse.setStatusMessage(authorizationResponse.statusMessage());
                            authorizationResponse.headers().forEach(h -> contextResponse.putHeader(h.getKey(), h.getValue()));
                            contextResponse.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                            contextResponse.end(buffer);
                        });
                    });
                    authorizationRequest
                        .putHeader(HttpHeaders.AUTHORIZATION, contextRequest.getHeader(HttpHeaders.AUTHORIZATION))
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .putHeader(HttpHeaders.ACCEPT, "application/json")
                        .end("grant_type=refresh_token&refresh_token=" + refreshToken);
                });

        };
    }

    /**
     * This handler passes the data through
     *
     * @return handler
     */
    public Handler<RoutingContext> unprotectedHandler(final String baseUri,
        final URI endpoint) {

        return context -> {
            final HttpServerRequest contextRequest = context.request();
            if (!contextRequest.uri().startsWith(baseUri)) {
                throw new IllegalStateException(contextRequest.uri() + " did not start with" + baseUri);
            }
            contextRequest.setExpectMultipart(context.parsedHeaders().contentType().isPermitted() && "multipart".equals(context.parsedHeaders().contentType().component()));
            final RequestOptions clientRequestOptions = Conversions.toRequestOptions(endpoint, contextRequest.uri().substring(baseUri.length()));
            final HttpClientRequest clientRequest = httpClient.request(contextRequest.method(), clientRequestOptions, clientResponse -> {
                contextRequest.response().setChunked(clientResponse.getHeader(HttpHeaders.CONTENT_LENGTH) == null)
                    .setStatusCode(clientResponse.statusCode())
                    .headers().setAll(clientResponse.headers());
                contextRequest.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                clientResponse.handler(contextRequest.response()::write)
                    .endHandler((v) -> contextRequest.response().end());
            }).exceptionHandler(context::fail)
                .setChunked(true);
            clientRequest.headers().setAll(contextRequest.headers());
            contextRequest.handler(data -> {
                clientRequest.write(data);
            })
                .endHandler((v) -> clientRequest.end());

        };
    }
}
