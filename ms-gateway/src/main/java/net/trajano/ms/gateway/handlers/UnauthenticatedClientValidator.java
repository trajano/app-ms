package net.trajano.ms.gateway.handlers;

import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_JSON;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Conversions;
import net.trajano.ms.gateway.internal.PathContext;
import net.trajano.ms.gateway.providers.GatewayClientAuthorization;

/**
 * <p>
 * Handles determining what Origin URIs are allowed based on the Origin header.
 * This will make sure that the client is allowed based on the Origin to see if
 * it is whitelisted.
 * </p>
 * This is done after ClientOriginHandler and checks if the client has already
 * been authorized there using the context data.
 *
 * @author Archimedes Trajano
 */
@Component
@Order(SelfRegisteringRoutingContextHandler.PROXIED)
public class UnauthenticatedClientValidator extends SelfRegisteringRoutingContextHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UnauthenticatedClientValidator.class);

    @Value("${authorization.endpoint}")
    private URI authorizationEndpoint;

    @Autowired
    private GatewayClientAuthorization gatewayClientAuthorization;

    @Autowired
    private HttpClient httpClient;

    @Override
    public void handle(final RoutingContext context) {

        final PathContext pathContext = getPathContext(context);
        if (pathContext == null || !pathContext.isProtected()) {
            context.next();
            return;
        }

        // Check if the client is already authorized
        final Boolean authorized = context.get("client_authorized");
        LOG.debug("authorized={}", authorized);
        if (Boolean.TRUE.equals(authorized)) {
            context.next();
            return;
        }

        // The origin was already set by the previous step.
        final URI originUri = context.get("origin");
        LOG.debug("originUri={} clientCheckEndpoint={}", originUri, authorizationEndpoint);
        final HttpClientRequest authorizationRequest = httpClient.post(Conversions.toRequestOptions(authorizationEndpoint.resolve("/check")), authorizationResponse -> {
            LOG.debug("statusCode={}", authorizationResponse.statusCode());
            if (authorizationResponse.statusCode() == 204) {
                context.next();
            } else {
                authorizationResponse.headers().forEach(h -> stripTransferEncodingAndLength(context.response(), h));
                authorizationResponse.bodyHandler(body -> {
                    context.response().setStatusCode(authorizationResponse.statusCode())
                        .setStatusMessage(authorizationResponse.statusMessage())
                        .setChunked(false)
                        .end(body);
                    LOG.debug("response={}", body);
                });
            }
        });
        final JsonObject checkRequest = new JsonObject();
        checkRequest.put("origin", originUri.toASCIIString());
        LOG.debug("checkRequest={}", checkRequest);
        gatewayClientAuthorization.gatewayRequest(authorizationRequest)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .putHeader(HttpHeaders.ACCEPT, APPLICATION_JSON)
            .end(checkRequest.toBuffer());
        context.request().pause();

    }

    /**
     * Register this only to the options() method handler. {@inheritDoc}
     */
    @Override
    public void register(final Router router) {

        router.route().handler(this);

    }

    private void stripTransferEncodingAndLength(final HttpServerResponse contextResponse,
        final Map.Entry<String, String> h) {

        if ("Content-Length".equalsIgnoreCase(h.getKey()) || "Transfer-Encoding".equalsIgnoreCase(h.getKey())) {
            return;
        }
        contextResponse.putHeader(h.getKey(), h.getValue());
    }
}
