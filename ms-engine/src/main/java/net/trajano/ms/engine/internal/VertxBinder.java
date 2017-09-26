package net.trajano.ms.engine.internal;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.springframework.context.ApplicationContext;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class VertxBinder extends AbstractBinder {

    private final ApplicationContext ctx;

    private final Vertx vertx;

    public VertxBinder(final Vertx vertx,
        final ApplicationContext ctx) {

        this.vertx = vertx;
        this.ctx = ctx;
    }

    @Override
    protected void configure() {

        bindFactory(RoutingContextFactory.class)
            .to(RoutingContext.class)
            .proxy(true)
            .proxyForSameScope(false)
            .in(RequestScoped.class);
        bind(vertx).to(Vertx.class);
        bind(ctx).to(ApplicationContext.class);

    }

}
