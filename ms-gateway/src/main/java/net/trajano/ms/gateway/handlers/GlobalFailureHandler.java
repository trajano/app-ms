package net.trajano.ms.gateway.handlers;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Errors;
import net.trajano.ms.gateway.internal.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.UnknownHostException;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_GLOBAL)
public class GlobalFailureHandler implements
    SelfRegisteringRoutingContextHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalFailureHandler.class);

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    private static final String GATEWAY_ERROR = "Gateway Error";

    private static final String GATEWAY_TIMEOUT = "Gateway Timeout";

    @Override
    public void handle(RoutingContext context) {

        LOG.error("Unhandled server exception", context.failure());
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
                context.response().setStatusCode(500)
                    .setStatusMessage(INTERNAL_SERVER_ERROR)
                    .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                    .end(Errors.serverError(INTERNAL_SERVER_ERROR).toBuffer());
            }
        }

    }

    @Override
    public void register(Router router) {

        router.route().failureHandler(this);
    }
}
