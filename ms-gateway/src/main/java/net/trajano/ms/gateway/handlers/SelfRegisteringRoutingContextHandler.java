package net.trajano.ms.gateway.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.ContextSettingHandler;
import net.trajano.ms.gateway.internal.PathContext;

/**
 * Defines a callback to handle the registrations. PostConstruct does not work
 * because order is not guaranteed.
 */
public interface SelfRegisteringRoutingContextHandler extends
    Handler<RoutingContext> {

    int CORE_GLOBAL = 0;

    int CORE_PATHS = 100;

    @Override
    default void handle(final RoutingContext context) {

        context.next();
    }

    /**
     * Obtains the path context from the {@link RoutingContext}.
     *
     * @param context
     *            RoutingContext
     * @return PathContext
     */
    default PathContext getPathContext(final RoutingContext context) {

        return context.get(ContextSettingHandler.PATH_CONTEXT);
    }

    /**
     * Register to router
     *
     * @param router
     */
    void register(Router router);
}
