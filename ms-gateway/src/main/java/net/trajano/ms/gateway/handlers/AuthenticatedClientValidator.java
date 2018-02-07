package net.trajano.ms.gateway.handlers;

import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_JSON;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import net.trajano.ms.gateway.internal.Conversions;
import net.trajano.ms.gateway.internal.Errors;
import net.trajano.ms.gateway.internal.MediaTypes;
import net.trajano.ms.gateway.internal.PathContext;
import net.trajano.ms.gateway.providers.GatewayClientAuthorization;

/**
 * <p>
 * Handles determining what Origin URIs are allowed based on the Origin and
 * Authorization header. If the authorization is Basic, then the client ID and
 * secret are verified against the system. If it is a Bearer authorization, it
 * will use the <code>aud</code> value of the data referenced by the access
 * token.
 * </p>
 *
 * @author Archimedes Trajano
 */
@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_GLOBAL + 1)
public class AuthenticatedClientValidator extends SelfRegisteringRoutingContextHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticatedClientValidator.class);

    /**
     * Strips off the path components from a URL.
     *
     * @param url
     *            URL to process
     * @return a URI suitable for Access-Control-Allow-Origin
     * @throws MalformedURLException
     *             invalid URL
     */
    public static URI getPartsForOriginHeader(final URL url) throws MalformedURLException {

        final String tempOriginString = new URL(url, "/").toString();
        return URI.create(tempOriginString.substring(0, tempOriginString.length() - 1));
    }

    @Value("${authorization.endpoint}")
    private URI authorizationEndpoint;

    @Autowired
    private GatewayClientAuthorization gatewayClientAuthorization;

    @Autowired
    private HttpClient httpClient;

    /**
     * Flag to check if Origin should be checked.
     */
    @Value("${client_validator.require_origin_check:true}")
    private boolean requireOriginCheck;

    @Override
    public void handle(final RoutingContext context) {

        final PathContext pathContext = getPathContext(context);
        if (pathContext == null || !pathContext.isProtected()) {
            context.next();
            return;
        }

        LOG.debug("Handling {} ended={} path is protected", context, context.request().isEnded());

        final String origin = context.request().getHeader(HttpHeaders.ORIGIN);
        final String referrer = context.request().getHeader(HttpHeaders.REFERER);

        final URI originUri;
        try {
            final URL tempOrigin;
            if (origin == null && referrer != null) {
                tempOrigin = new URL(referrer);
            } else {
                tempOrigin = new URL(origin);
            }
            originUri = getPartsForOriginHeader(tempOrigin);
        } catch (final MalformedURLException e) {
            context.response().setStatusCode(400)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                .end(Errors.missingOrigin().toBuffer());

            return;
        }
        LOG.debug("originUri={}", originUri);
        context.put("origin", originUri);

        final String authorization = context.request().getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            context.next();
            return;
        }

        LOG.debug("context={} authorization={}", context, authorization);

        final HttpClientRequest authorizationRequest = httpClient.post(Conversions.toRequestOptions(authorizationEndpoint.resolve("/check")), authorizationResponse -> {
            LOG.debug("statusCode={}", authorizationResponse.statusCode());
            if (authorizationResponse.statusCode() == 204) {
                context.put("client_authorized", true);
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
        checkRequest.put("authorization", authorization);
        checkRequest.put("origin", originUri.toASCIIString());
        LOG.debug("checkRequest={}", checkRequest);
        gatewayClientAuthorization.gatewayRequest(authorizationRequest)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .putHeader(HttpHeaders.ACCEPT, APPLICATION_JSON)
            .end(checkRequest.toBuffer());
        context.request().pause();

    }

    /**
     * Register this but also register the {@link CorsHandler}. The
     * {@link CorsHandler} will deal with the normal CORS headers after it has been
     * processed initially by this handler. {@inheritDoc}
     */
    @Override
    public void register(final Router router) {

        router.route().handler(this);

        if (requireOriginCheck) {
            router.route().handler(CorsHandler.create(".+")
                .maxAgeSeconds(600)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("Content-Type")
                .allowedHeader("Accept")
                .allowedHeader("Accept-Language")
                .allowedHeader("Authorization"));
        } else {
            router.route().handler(CorsHandler.create("*")
                .maxAgeSeconds(600)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("Content-Type")
                .allowedHeader("Accept")
                .allowedHeader("Accept-Language")
                .allowedHeader("Authorization"));
        }

    }

    private void stripTransferEncodingAndLength(final HttpServerResponse contextResponse,
        final Map.Entry<String, String> h) {

        if ("Content-Length".equalsIgnoreCase(h.getKey()) || "Transfer-Encoding".equalsIgnoreCase(h.getKey())) {
            return;
        }
        contextResponse.putHeader(h.getKey(), h.getValue());
    }
}
