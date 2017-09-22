package net.trajano.ms.engine.internal;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class VertxRequestBinder extends AbstractBinder {

    private final RoutingContext routingContext;

    private final Vertx vertx;

    public VertxRequestBinder(final RoutingContext routingContext,
        final Vertx vertx) {

        this.routingContext = routingContext;
        this.vertx = vertx;
    }

    @Override
    protected void configure() {

        System.out.println("vertx.getOrCreateContext()" + vertx.getOrCreateContext());
        System.out.println(routingContext);
        bind(routingContext).to(RoutingContext.class);
        bind(routingContext.request()).to(HttpServerRequest.class);

    }

}
