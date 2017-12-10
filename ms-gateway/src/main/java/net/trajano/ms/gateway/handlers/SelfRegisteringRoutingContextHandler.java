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
public abstract class SelfRegisteringRoutingContextHandler implements
    Handler<RoutingContext> {

    public static final int CORE_GLOBAL = 0;

    public static final int CORE_PATHS = 100;

    public static final int PROXIED = 1000;

    /**
     * Obtains the path context from the {@link RoutingContext}.
     *
     * @param context
     *            RoutingContext
     * @return PathContext
     */
    protected PathContext getPathContext(final RoutingContext context) {

        return context.get(ContextSettingHandler.PATH_CONTEXT);
    }

    /**
     * Register to router
     *
     * @param router
     *            router
     */
    public abstract void register(Router router);
}
