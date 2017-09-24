package net.trajano.ms.engine.internal;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

@Provider
public class RoutingContextFactory implements
    Supplier<RoutingContext> {

    @Inject
    private Vertx vertx;

    @Override
    public RoutingContext get() {

        return (RoutingContext) vertx.getOrCreateContext().get(RoutingContext.class.getName());
    }
}
