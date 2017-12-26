package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.WriterInterceptor;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponseWriter;
import org.jboss.resteasy.specimpl.BuiltResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.RoutingContext;

public class VertxAsynchronousResponse implements
    ResteasyAsynchronousResponse {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(VertxAsynchronousResponse.class);

    private Annotation[] annotations;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * Completion callbacks.
     */
    private final List<CompletionCallback> completionCallbacks = new LinkedList<>();

    private final AtomicBoolean done = new AtomicBoolean(false);

    private ResourceMethodInvoker invoker;

    /**
     * RestEasy provider factory.
     */
    private final ResteasyProviderFactory providerFactory;

    private final HttpRequest request;

    private ContainerResponseFilter[] responseFilters;

    /**
     * Vert.X routing context.
     */
    private final RoutingContext routingContext;

    private final AtomicBoolean suspended = new AtomicBoolean(true);

    /**
     * JAX RS Timeout Handler.
     */
    private TimeoutHandler timeoutHandler;

    private long timeoutTimerID = -1;

    private final Semaphore writeLock = new Semaphore(1);

    private WriterInterceptor[] writerInterceptors;

    public VertxAsynchronousResponse(final ResteasyProviderFactory providerFactory,
        final HttpRequest request,
        final RoutingContext routingContext) {

        this.providerFactory = providerFactory;
        this.request = request;
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

    /**
     * Cancels the current timeout timer if defined.
     */
    private void cancelTimer() {

        if (timeoutTimerID != -1 && !routingContext.vertx().cancelTimer(timeoutTimerID)) {
            LOG.error("Attempted to cancel a timer that does not exist {}", timeoutTimerID);
        }
    }

    @Override
    public Annotation[] getAnnotations() {

        return annotations;
    }

    @Override
    public ResourceMethodInvoker getMethod() {

        return invoker;
    }

    @Override
    public ContainerResponseFilter[] getResponseFilters() {

        return responseFilters;
    }

    @Override
    public WriterInterceptor[] getWriterInterceptors() {

        return writerInterceptors;
    }

    private void handleTimeout() {

        LOG.warn("Timeout has occurred for timerId={}", timeoutTimerID);
        timeoutTimerID = -1;
        if (timeoutHandler != null) {
            timeoutHandler.handleTimeout(this);
        }

    }

    /**
     * {@inheritDoc}. This system supports async HTTP and this is a noop.
     */
    @Override
    public void initialRequestThreadFinished() {

        // does nothing
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
     * Not used. {@inheritDoc}
     *
     * @return empty set.
     */
    @Override
    public Collection<Class<?>> register(final Class<?> callback) {

        return Collections.emptySet();
    }

    /**
     * Not used. {@inheritDoc}
     *
     * @return empty map.
     */
    @Override
    public Map<Class<?>, Collection<Class<?>>> register(final Class<?> callback,
        final Class<?>... callbacks) {

        return Collections.emptyMap();
    }

    /**
     * Not used. {@inheritDoc}
     *
     * @return empty set.
     */
    @Override
    public Collection<Class<?>> register(final Object callback) {

        return Collections.emptySet();
    }

    /**
     * Not used. {@inheritDoc}
     *
     * @return empty map.
     */
    @Override
    public Map<Class<?>, Collection<Class<?>>> register(final Object callback,
        final Object... callbacks) {

        return Collections.emptyMap();
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

            final Response response;
            if (entity == null) {
                response = Response.noContent().build();
            } else if (entity instanceof Response) {
                response = (Response) entity;
            } else {
                response = Response.ok(entity, invoker.resolveContentType(request, entity)).build();
            }

            writeResponse(response);

            done.set(true);
            cancelTimer();
            completionCallbacks.forEach(callback -> callback.onComplete(null));
            return true;
        } catch (final IOException e) {
            throw new InternalServerErrorException(e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            writeLock.release();
        }
    }

    @Override
    public void setAnnotations(final Annotation[] annotations) {

        this.annotations = annotations;
    }

    /**
     * Set the resource method invoker.
     */
    @Override
    public void setMethod(final ResourceMethodInvoker invoker) {

        this.invoker = invoker;

    }

    @Override
    public void setResponseFilters(final ContainerResponseFilter[] responseFilters) {

        this.responseFilters = responseFilters;
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
            cancelTimer();
            final long millis = unit.toMillis(time);
            timeoutTimerID = routingContext.vertx().setTimer(millis, timerId -> handleTimeout());
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

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setTimeoutHandler(final TimeoutHandler handler) {

        timeoutHandler = handler;

    }

    @Override
    public void setWriterInterceptors(final WriterInterceptor[] writerInterceptors) {

        this.writerInterceptors = writerInterceptors;

    }

    /**
     * Write the response.
     *
     * @param response
     *            response
     * @throws IOException
     */
    private void writeResponse(final Response response) throws IOException {

        ServerResponseWriter.writeNomapResponse((BuiltResponse) response, request, new VertxHttpResponse(routingContext), providerFactory);

    }
}
