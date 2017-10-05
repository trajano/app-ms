package net.trajano.ms.engine.internal.resteasy;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ServiceUnavailableException;

import org.jboss.resteasy.core.AbstractAsynchronousResponse;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyAsynchronousResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class VertxExecutionContext implements
    ResteasyAsynchronousContext {

    private static class AsynchronousResponse extends AbstractAsynchronousResponse {

        private boolean cancelled = false;

        private boolean done;

        private final Future<Object> future;

        private boolean suspended = true;

        private long suspendTimerId;

        private boolean timedout;

        private final Vertx vertx;

        public AsynchronousResponse(final SynchronousDispatcher dispatcher,
            final HttpRequest request,
            final HttpResponse response,
            final Future<Object> future,
            final Vertx vertx) {

            super(dispatcher, request, response);
            this.future = future;
            this.vertx = vertx;
        }

        @Override
        public boolean cancel() {

            return sendCancel(new ServiceUnavailableException());
        }

        @Override
        public boolean cancel(final Date retryAfter) {

            return sendCancel(new ServiceUnavailableException(retryAfter));
        }

        @Override
        public boolean cancel(final int retryAfter) {

            return sendCancel(new ServiceUnavailableException((long) retryAfter));
        }

        private void handleTimeout() {

            LOG.debug("Suspend timeout has occured in response timerId={}", suspendTimerId);
            timedout = true;
            cancel();

        }

        @Override
        public void initialRequestThreadFinished() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCancelled() {

            return cancelled;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDone() {

            return done || cancelled || timedout;
        }

        @Override
        public boolean isSuspended() {

            return suspended;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean resume(final Object response) {

            if (!suspended) {
                return false;
            }
            suspended = false;
            vertx.cancelTimer(suspendTimerId);
            internalResume(response);
            future.complete(response);
            return true;
        }

        @Override
        public boolean resume(final Throwable response) {

            if (!suspended) {
                return false;
            }
            suspended = false;
            vertx.cancelTimer(suspendTimerId);
            internalResume(response);
            future.fail(response);
            return true;
        }

        private boolean sendCancel(final ServiceUnavailableException exception) {

            if (!suspended) {
                return false;
            }
            vertx.cancelTimer(suspendTimerId);

            internalResume(exception.getResponse());
            suspended = false;
            cancelled = true;
            return false;
        }

        @Override
        public boolean setTimeout(final long time,
            final TimeUnit unit) {

            if (done) {
                return false;
            }
            suspendTimerId = vertx.setTimer(unit.toMillis(time), timerId -> {
                handleTimeout();
            });

            return true;
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(VertxExecutionContext.class);

    private final AsynchronousResponse asynchronousResponse;

    private final Future<Object> future;

    private final RoutingContext routingContext;

    private boolean suspended;

    private long suspendTimerId;

    public VertxExecutionContext(final RoutingContext routingContext,
        final SynchronousDispatcher dispatcher,
        final HttpRequest request,
        final HttpResponse response) {

        this.routingContext = routingContext;
        future = Future.future();
        asynchronousResponse = new AsynchronousResponse(dispatcher, request, response, future, routingContext.vertx());
    }

    @Override
    public ResteasyAsynchronousResponse getAsyncResponse() {

        return asynchronousResponse;
    }

    private void handleTimeout() {

        LOG.debug("Suspend timeout has occured timerId={}", suspendTimerId);

    }

    @Override
    public boolean isSuspended() {

        return suspended;
    }

    @Override
    public ResteasyAsynchronousResponse suspend() throws IllegalStateException {

        suspended = true;
        return asynchronousResponse;
    }

    @Override
    public ResteasyAsynchronousResponse suspend(final long millis) throws IllegalStateException {

        suspendTimerId = routingContext.vertx().setTimer(millis, timerId -> {
            handleTimeout();
        });
        return asynchronousResponse;
    }

    @Override
    public ResteasyAsynchronousResponse suspend(final long time,
        final TimeUnit unit) throws IllegalStateException {

        return suspend(unit.toMillis(time));

    }

}
