package net.trajano.ms.engine.internal;

import javax.enterprise.context.RequestScoped;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import io.vertx.ext.web.RoutingContext;

public class VertxBinder extends AbstractBinder {

    @Override
    protected void configure() {

        bindFactory(RoutingContextFactory.class)
            .to(RoutingContext.class)
            .proxy(true)
            .proxyForSameScope(false)
            .in(RequestScoped.class);
        //        bindFactory(RoutingContextFactory.class)
        //            .to(HttpServerRequest.class)
        //            .proxy(true)
        //            .proxyForSameScope(false)
        //            .in(RequestScoped.class);
        //        bind(vertx.getOrCreateContext()).to(Context.class);
        //        bind(vertx).to(Vertx.class);

    }

}
