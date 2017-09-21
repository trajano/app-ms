package net.trajano.ms.engine.internal;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class VertxBinder extends AbstractBinder {

    private final Context context;

    private final Vertx vertx;

    public VertxBinder(final Vertx vertx) {

        this.vertx = vertx;
        context = vertx.getOrCreateContext();

    }

    @Override
    protected void configure() {

        bind(context).to(Context.class);
        bind(vertx).to(Vertx.class);

    }

}
