package net.trajano.ms.gateway.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Defines a callback to handle the registrations. PostConstruct does not work
 * because order is not guaranteed.
 */
public interface SelfRegisteringRoutingContextHandler extends
    Handler<RoutingContext> {

    int CORE_GLOBAL = 0;

    int CORE_PATHS = 100;

    @Override
    default void handle(RoutingContext context) {

        context.next();
    }

    /**
     * Register to router
     * 
     * @param router
     */
    void register(Router router);
}
