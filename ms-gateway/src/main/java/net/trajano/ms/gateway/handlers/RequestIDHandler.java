package net.trajano.ms.gateway.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.providers.RequestIDProvider;

/**
 * Adds the request ID data to the context and the response message.
 *
 * @author Archimedes Trajano
 */
@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_GLOBAL)
public class RequestIDHandler extends SelfRegisteringRoutingContextHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RequestIDHandler.class);

    @Autowired
    private RequestIDProvider requestIDProvider;

    @Override
    public void handle(final RoutingContext context) {

        final String requestID = requestIDProvider.newRequestID(context);
        if (LOG.isDebugEnabled()) {
            LOG.debug("requestID={}", requestID);
            context.request().headers().forEach(h -> {
                LOG.debug("header {}={}", h.getKey(), h.getValue());
            });
        }
        context.next();
    }

    @Override
    public void register(final Router router) {

        router.route().handler(this);
    }
}
