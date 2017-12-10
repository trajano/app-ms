package net.trajano.ms.gateway.internal;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Context setting handler. This will set the path information that would be
 * associated with route into the context so it can be used by the Unprotected
 * and Protected handlers
 *
 * @author Archimedes Trajano
 */
public class ContextSettingHandler implements
    Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(ContextSettingHandler.class);

    public static final String PATH_CONTEXT = "path_context";

    private final PathContext pathContext;

    public ContextSettingHandler(final boolean protect,
        final String from,
        final URI to,
        final long limit) {

        pathContext = new PathContext(protect, from, to, limit);
    }

    @Override
    public void handle(final RoutingContext c) {

        LOG.debug("Handling {} with from={} to={} protected={}", c, pathContext.getFrom(), pathContext.getTo(), pathContext.isProtected());
        c.put(PATH_CONTEXT, pathContext);
        c.next();
    }

}
