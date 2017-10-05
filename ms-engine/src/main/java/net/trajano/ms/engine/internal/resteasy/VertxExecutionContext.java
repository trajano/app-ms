package net.trajano.ms.engine.internal.resteasy;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.core.AbstractAsynchronousResponse;
import org.jboss.resteasy.core.AbstractExecutionContext;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyAsynchronousResponse;

import io.vertx.core.Vertx;

public class VertxExecutionContext extends AbstractExecutionContext {

    private static class AsynchronousResponse extends AbstractAsynchronousResponse {

        public AsynchronousResponse(final SynchronousDispatcher dispatcher,
            final HttpRequest request,
            final HttpResponse response) {

            super(dispatcher, request, response);
        }

        @Override
        public boolean cancel() {

            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean cancel(final Date retryAfter) {

            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean cancel(final int retryAfter) {

            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void initialRequestThreadFinished() {

            // TODO Auto-generated method stub

        }

        @Override
        public boolean isCancelled() {

            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isDone() {

            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isSuspended() {

            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean resume(final Object response) {

            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean resume(final Throwable response) {

            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean setTimeout(final long time,
            final TimeUnit unit) {

            // TODO Auto-generated method stub
            return false;
        }

    }

    private final AsynchronousResponse asynchronousResponse;

    private final SynchronousDispatcher dispatcher;

    private final HttpRequest request;

    private final HttpResponse response;

    private boolean suspended;

    private Vertx vertx;

    public VertxExecutionContext(final SynchronousDispatcher dispatcher,
        final HttpRequest request,
        final HttpResponse response) {

        super(dispatcher, request, response);
        this.dispatcher = dispatcher;
        this.request = request;
        this.response = response;
        asynchronousResponse = new AsynchronousResponse(dispatcher, request, response);
    }

    @Override
    public ResteasyAsynchronousResponse getAsyncResponse() {

        return asynchronousResponse;
    }

    @Override
    public boolean isSuspended() {

        return suspended;
    }

    @Override
    public ResteasyAsynchronousResponse suspend() throws IllegalStateException {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResteasyAsynchronousResponse suspend(final long millis) throws IllegalStateException {

        return suspend(millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public ResteasyAsynchronousResponse suspend(final long time,
        final TimeUnit unit) throws IllegalStateException {

        vertx.setTimer(time, timerId -> {

        });
        return null;
    }

}
