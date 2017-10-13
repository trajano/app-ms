package net.trajano.ms.engine.internal.resteasy;

import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.core.AbstractAsynchronousResponse;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.RoutingContext;

public class VertxAsynchronousResponse extends AbstractAsynchronousResponse {

    private static final Logger LOG = LoggerFactory.getLogger(VertxAsynchronousResponse.class);

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private final AtomicBoolean done = new AtomicBoolean(false);

    private final RoutingContext routingContext;

    private final AtomicBoolean suspended = new AtomicBoolean(true);

    private long timeoutTimerID = -1;

    private final Semaphore writeLock = new Semaphore(1);

    public VertxAsynchronousResponse(final SynchronousDispatcher dispatcher,
        final HttpRequest request,
        final VertxHttpResponse response,
        final RoutingContext routingContext) {

        super(dispatcher, request, response);
        this.routingContext = routingContext;
    }

    @Override
    public boolean cancel() {

        if (cancelled.getAndSet(true)) {
            return false;
        }
        return sendData(Response.ok(Status.SERVICE_UNAVAILABLE.getReasonPhrase())
            .status(Status.SERVICE_UNAVAILABLE).build());
    }

    @Override
    public boolean cancel(final Date retryAfter) {

        if (cancelled.getAndSet(true)) {
            return false;
        }
        return sendData(Response.ok(Status.SERVICE_UNAVAILABLE.getReasonPhrase())
            .header(HttpHeaders.RETRY_AFTER, retryAfter)
            .status(Status.SERVICE_UNAVAILABLE).build());
    }

    @Override
    public boolean cancel(final int retryAfter) {

        if (cancelled.getAndSet(true)) {
            return false;
        }
        return sendData(Response.ok(Status.SERVICE_UNAVAILABLE.getReasonPhrase())
            .header(HttpHeaders.RETRY_AFTER, retryAfter)
            .status(Status.SERVICE_UNAVAILABLE).build());
    }

    private void handleTimeout() {

        LOG.warn("Timeout has occurred for timerId={}", timeoutTimerID);
        timeoutTimerID = -1;
        if (timeoutHandler != null) {
            timeoutHandler.handleTimeout(this);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialRequestThreadFinished() {

        LOG.debug("initialRequestThreadFinished");

    }

    @Override
    public boolean isCancelled() {

        return cancelled.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {

        return done.get();
    }

    @Override
    public boolean isSuspended() {

        return suspended.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean resume(final Object response) {

        LOG.debug("Object response received={}", response);
        return sendData(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean resume(final Throwable response) {

        LOG.debug("Throwable response received={}", response);
        return sendData(response);
    }

    private boolean sendData(final Object entity) {

        if (!suspended.getAndSet(false)) {
            return false;
        }

        try {
            if (!writeLock.tryAcquire()) {
                LOG.error("Semaphore locked", timeoutTimerID);
                writeLock.acquire();
            }
            internalResume(entity);
            done.set(true);
            if (timeoutTimerID != -1) {
                if (!routingContext.vertx().cancelTimer(timeoutTimerID)) {
                    LOG.error("Attempted to cancel a timer that does not exist {}", timeoutTimerID);
                }
            }
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            writeLock.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setTimeout(final long time,
        final TimeUnit unit) {

        try {
            writeLock.acquire();
            if (!isSuspended()) {
                return false;
            }
            if (timeoutTimerID != -1) {
                if (!routingContext.vertx().cancelTimer(timeoutTimerID)) {
                    LOG.error("Attempted to cancel a timer that does not exist {}", timeoutTimerID);
                }
            }
            final long millis = unit.toMillis(time);
            timeoutTimerID = routingContext.vertx().setTimer(millis, timerId -> {
                handleTimeout();
            });
            LOG.debug("New timeout handler created timeoutTimerId={} for {} ms", timeoutTimerID, millis);
            return true;
        } catch (final InterruptedException e) {
            routingContext.fail(e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            writeLock.release();
        }
    }

}
