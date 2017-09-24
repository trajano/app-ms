package net.trajano.ms.engine.internal;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class VertxBinder extends AbstractBinder {

    private final Vertx vertx;

    public VertxBinder(final Vertx vertx) {

        this.vertx = vertx;
    }

    @Override
    protected void configure() {

        bindFactory(RoutingContextFactory.class)
            .to(RoutingContext.class)
            .proxy(true)
            .proxyForSameScope(false)
            .in(RequestScoped.class);
        bind(vertx).to(Vertx.class);

    }

}
