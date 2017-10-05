package net.trajano.ms.engine.internal.resteasy;

import java.util.Date;
import java.util.concurrent.Semaphore;
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

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class VertxExecutionContext implements
    ResteasyAsynchronousContext {

    private static class AsynchronousResponse extends AbstractAsynchronousResponse {

        private boolean cancelled = false;

        private boolean done;

        /**
         * Prevent access from multiple threads.
         */
        private final Semaphore lock = new Semaphore(1);

        private final RoutingContext routingContext;

        private boolean suspended = true;

        private long suspendTimerId;

        private boolean timedout;

        private final Vertx vertx;

        public AsynchronousResponse(final SynchronousDispatcher dispatcher,
            final HttpRequest request,
            final HttpResponse response,
            final RoutingContext routingContext) {

            super(dispatcher, request, response);
            this.routingContext = routingContext;
            vertx = routingContext.vertx();
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

            LOG.error("Suspend timeout has occured in response timerId={}", suspendTimerId);
            if (timeoutHandler != null) {
                timeoutHandler.handleTimeout(this);
            }
            timedout = true;
            cancel();

        }

        /**
         * {@inheritDoc}
         */
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

            try {
                lock.acquire();

                if (!suspended || isDone()) {
                    return false;
                }
                suspended = false;
                vertx.cancelTimer(suspendTimerId);
                internalResume(response);
                return true;
            } catch (final InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException(e);
            } finally {
                lock.release();
                routingContext.response().end();

            }
        }

        @Override
        public boolean resume(final Throwable response) {

            try {
                lock.acquire();

                if (!suspended || isDone()) {
                    return false;
                }
                suspended = false;
                vertx.cancelTimer(suspendTimerId);
                internalResume(response);
                return true;
            } catch (final InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException(e);
            } finally {
                lock.release();
            }

        }

        private boolean sendCancel(final ServiceUnavailableException exception) {

            try {
                lock.acquire();

                if (!suspended || isDone()) {
                    return false;
                }
                vertx.cancelTimer(suspendTimerId);

                internalResume(exception.getResponse());
                suspended = false;
                cancelled = true;
                return true;
            } catch (final InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException(e);
            } finally {
                lock.release();
            }
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

    private AsynchronousResponse asynchronousResponse;

    private final SynchronousDispatcher dispatcher;

    private final HttpRequest request;

    private final HttpResponse response;

    private final RoutingContext routingContext;

    public VertxExecutionContext(final RoutingContext routingContext,
        final SynchronousDispatcher dispatcher,
        final HttpRequest request,
        final HttpResponse response) {

        this.routingContext = routingContext;
        this.request = request;
        this.response = response;
        this.dispatcher = dispatcher;
    }

    @Override
    public ResteasyAsynchronousResponse getAsyncResponse() {

        return asynchronousResponse;
    }

    @Override
    public boolean isSuspended() {

        return asynchronousResponse != null;
    }

    @Override
    public ResteasyAsynchronousResponse suspend() throws IllegalStateException {

        asynchronousResponse = new AsynchronousResponse(dispatcher, request, response, routingContext);
        return asynchronousResponse;
    }

    @Override
    public ResteasyAsynchronousResponse suspend(final long millis) throws IllegalStateException {

        return suspend(millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public ResteasyAsynchronousResponse suspend(final long time,
        final TimeUnit unit) throws IllegalStateException {

        asynchronousResponse = new AsynchronousResponse(dispatcher, request, response, routingContext);
        asynchronousResponse.setTimeout(time, unit);
        return asynchronousResponse;

    }

}
