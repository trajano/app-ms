package net.trajano.ms.gateway.handlers;

import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import net.trajano.ms.gateway.internal.Conversions;
import net.trajano.ms.gateway.providers.GatewayClientAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_JSON;

@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_PATHS + 1)
public class ClientOriginHandler implements
    SelfRegisteringRoutingContextHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ClientOriginHandler.class);

    @Autowired
    private HttpClient httpClient;

    @Value("${authorization.client_check_endpoint}")
    private URI clientCheckEndpoint;

    @Autowired
    private GatewayClientAuthorization gatewayClientAuthorization;

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

    @Override
    public void handle(RoutingContext context) {

        if (context.request().method() == HttpMethod.OPTIONS) {
            context.next();
            return;
        }
        final String authorization = context.request().getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            context.next();
            return;
        }

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
            context.fail(400);
            return;
        }
        LOG.debug("originUri={}", originUri);
        LOG.debug("clientCheckEndpoint={}", clientCheckEndpoint);
        final HttpClientRequest authorizationRequest = httpClient.post(Conversions.toRequestOptions(clientCheckEndpoint), authorizationResponse -> {
            LOG.debug("statusCode={}", authorizationResponse.statusCode());
            context.request().resume();
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
        checkRequest.put("authorization", authorization);
        checkRequest.put("origin", originUri.toASCIIString());
        LOG.debug("checkRequest={}", checkRequest);
        gatewayClientAuthorization.gatewayRequest(authorizationRequest)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .putHeader(HttpHeaders.ACCEPT, APPLICATION_JSON)
            .end(checkRequest.toBuffer());
        context.request().pause();

    }

    private void stripTransferEncodingAndLength(final HttpServerResponse contextResponse,
        final Map.Entry<String, String> h) {

        if ("Content-Length".equalsIgnoreCase(h.getKey()) || "Transfer-Encoding".equalsIgnoreCase(h.getKey())) {
            return;
        }
        contextResponse.putHeader(h.getKey(), h.getValue());
    }

    @Override
    public void register(Router router) {

        router.route().handler(this);

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

    }
}
