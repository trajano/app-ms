package net.trajano.ms.engine.internal.resteasy;

import io.vertx.ext.web.RoutingContext;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponseWriter;
import org.jboss.resteasy.specimpl.BuiltResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.WriterInterceptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class VertxAsynchronousResponse implements
    ResteasyAsynchronousResponse {

    private static final Logger LOG = LoggerFactory.getLogger(VertxAsynchronousResponse.class);

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private final AtomicBoolean done = new AtomicBoolean(false);

    private final RoutingContext routingContext;

    private final AtomicBoolean suspended = new AtomicBoolean(true);

    /**
     * Completion callbacks.
     */
    private final List<CompletionCallback> completionCallbacks = new LinkedList<>();

    /**
     * RestEasy dispatcher.
     */
    private final Dispatcher dispatcher;

    private final HttpRequest request;

    private ResourceMethodInvoker invoker;

    private final Semaphore writeLock = new Semaphore(1);

    private long timeoutTimerID = -1;

    /**
     * JAX RS Timeout Handler.
     */
    private TimeoutHandler timeoutHandler;

    public VertxAsynchronousResponse(final Dispatcher dispatcher,
        final HttpRequest request,
        RoutingContext routingContext) {

        this.dispatcher = dispatcher;
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

    private void handleTimeout() {

        LOG.warn("Timeout has occurred for timerId={}", timeoutTimerID);
        timeoutTimerID = -1;
        if (timeoutHandler != null) {
            timeoutHandler.handleTimeout(this);
        }

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

    private void writeResponse(Response response) throws IOException {

        ServerResponseWriter.writeNomapResponse((BuiltResponse) response, request, new VertxHttpResponse(routingContext), dispatcher.getProviderFactory());
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
    public void setTimeoutHandler(TimeoutHandler handler) {

        this.timeoutHandler = handler;
    }

    /**
     * Not used. {@inheritDoc}
     *
     * @return empty set.
     */
    @Override
    public Collection<Class<?>> register(Class<?> callback) {

        return Collections.emptySet();
    }

    /**
     * Not used. {@inheritDoc}
     *
     * @return empty map.
     */
    @Override
    public Map<Class<?>, Collection<Class<?>>> register(Class<?> callback,
        Class<?>... callbacks) {

        return Collections.emptyMap();
    }

    /**
     * Not used. {@inheritDoc}
     *
     * @return empty set.
     */
    @Override
    public Collection<Class<?>> register(Object callback) {

        return Collections.emptySet();
    }

    /**
     * Not used. {@inheritDoc}
     *
     * @return empty map.
     */
    @Override
    public Map<Class<?>, Collection<Class<?>>> register(Object callback,
        Object... callbacks) {

        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}. This system supports async HTTP and this is a noop.
     */
    @Override
    public void initialRequestThreadFinished() {

        // does nothing
    }

    @Override
    public ContainerResponseFilter[] getResponseFilters() {

        return responseFilters;
    }

    @Override
    public void setResponseFilters(ContainerResponseFilter[] responseFilters) {

        this.responseFilters = responseFilters;
    }

    private ContainerResponseFilter[] responseFilters;

    @Override
    public WriterInterceptor[] getWriterInterceptors() {

        return writerInterceptors;
    }

    @Override
    public void setWriterInterceptors(WriterInterceptor[] writerInterceptors) {

        this.writerInterceptors = writerInterceptors;
    }

    private WriterInterceptor[] writerInterceptors;

    @Override
    public Annotation[] getAnnotations() {

        return annotations;
    }

    @Override
    public void setAnnotations(Annotation[] annotations) {

        this.annotations = annotations;
    }

    private Annotation[] annotations;

    @Override
    public ResourceMethodInvoker getMethod() {

        return invoker;
    }

    @Override
    public void setMethod(ResourceMethodInvoker invoker) {

        this.invoker = invoker;
    }
}
