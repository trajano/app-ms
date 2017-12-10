package net.trajano.ms.gateway.internal;

import java.net.URI;

import io.vertx.ext.web.RoutingContext;

public class PathContext {

    /**
     * Extract the {@link PathContext} from the {@link RoutingContext}.
     *
     * @param routingContext
     *            routing context
     * @return path context.
     */
    public static PathContext get(final RoutingContext routingContext) {

        return routingContext.get(ContextSettingHandler.PATH_CONTEXT);
    }

    private final String from;

    /**
     * Body size limit.
     */
    private final long limit;

    /**
     * Flag to check if the path is a protected.
     */
    private final boolean protect;

    private final URI to;

    public PathContext(final boolean protect,
        final String from,
        final URI to,
        final long limit) {

        this.protect = protect;
        this.from = from;
        this.to = to;
        this.limit = limit;
    }

    public String getFrom() {

        return from;
    }

    public long getLimit() {

        return limit;
    }

    public URI getTo() {

        return to;
    }

    /**
     * Checks if the path is protected.
     *
     * @return true if the path is protected.
     */
    public boolean isProtected() {

        return protect;
    }
}
