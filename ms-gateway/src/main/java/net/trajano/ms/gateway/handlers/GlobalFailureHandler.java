package net.trajano.ms.gateway.handlers;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import java.net.ConnectException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Errors;
import net.trajano.ms.gateway.internal.MediaTypes;

/**
 * This provides the error handling for those requests that have not been
 * handled correctly in the upstream routes.
 *
 * @author Archimedes Trajano
 */
@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_GLOBAL)
public class GlobalFailureHandler extends SelfRegisteringRoutingContextHandler {

    private static final String GATEWAY_ERROR = "Gateway Error";

    private static final String GATEWAY_TIMEOUT = "Gateway Timeout";

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    private static final Logger LOG = LoggerFactory.getLogger(GlobalFailureHandler.class);

    @Override
    public void handle(final RoutingContext context) {

        if (LOG.isErrorEnabled()) {
            LOG.error("Unhandled server exception statusCode={} responseEnded={} uri={}", context.statusCode(), context.response().ended(), context.request().uri(), context.failure());
        }
        if (!context.response().ended()) {
            if (context.failure() instanceof ConnectException) {
                context.response().setStatusCode(504)
                    .setStatusMessage(GATEWAY_TIMEOUT)
                    .putHeader(CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                    .end(Errors.serverError(GATEWAY_TIMEOUT).toBuffer());
            } else if (context.failure() instanceof UnknownHostException) {
                context.response().setStatusCode(503)
                    .setStatusMessage(GATEWAY_ERROR)
                    .putHeader(CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                    .end(Errors.serverError(GATEWAY_ERROR).toBuffer());
            } else {
                context.response().setStatusCode(context.statusCode())
                    .setStatusMessage(INTERNAL_SERVER_ERROR)
                    .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                    .end(Errors.serverError(INTERNAL_SERVER_ERROR).toBuffer());
            }
        }

    }

    @Override
    public void register(final Router router) {

        router.route().failureHandler(this);
    }
}
