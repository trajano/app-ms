package net.trajano.ms.engine.internal.resteasy;

import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyAsynchronousResponse;

import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.sample.VertxAsynchronousResponse;

public class VertxAsynchronousContext implements
    ResteasyAsynchronousContext {

    private final VertxAsynchronousResponse asyncResponse;

    private final RoutingContext context;

    private boolean suspended;

    public VertxAsynchronousContext(final RoutingContext context,
        final SynchronousDispatcher dispatcher) {

        this.context = context;
        asyncResponse = new VertxAsynchronousResponse(context.response());
    }

    @Override
    public ResteasyAsynchronousResponse getAsyncResponse() {

        return asyncResponse;
    }

    @Override
    public boolean isSuspended() {

        return suspended;
    }

    @Override
    public ResteasyAsynchronousResponse suspend() throws IllegalStateException {

        throw new UnsupportedOperationException();

    }

    @Override
    public ResteasyAsynchronousResponse suspend(final long millis) throws IllegalStateException {

        throw new UnsupportedOperationException();
    }

    @Override
    public ResteasyAsynchronousResponse suspend(final long time,
        final TimeUnit unit) throws IllegalStateException {

        throw new UnsupportedOperationException();
    }

}
