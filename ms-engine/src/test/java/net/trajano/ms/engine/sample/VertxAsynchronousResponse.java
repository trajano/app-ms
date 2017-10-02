package net.trajano.ms.engine.sample;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.ext.WriterInterceptor;

import org.jboss.resteasy.core.AbstractAsynchronousResponse;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.SynchronousDispatcher;

import io.vertx.core.http.HttpServerResponse;

public class VertxAsynchronousResponse extends AbstractAsynchronousResponse {

    private final HttpServerResponse vertxResponse;

    public VertxAsynchronousResponse(final HttpServerResponse vertxResponse,
        final SynchronousDispatcher dispatcher) {

        super(dispatcher);
        this.vertxResponse = vertxResponse;
    }

    @Override
    public boolean cancel() {

        if (vertxResponse.ended()) {
            return false;
        }
        vertxResponse.end();
        return true;
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
    public Annotation[] getAnnotations() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceMethodInvoker getMethod() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContainerResponseFilter[] getResponseFilters() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WriterInterceptor[] getWriterInterceptors() {

        // TODO Auto-generated method stub
        return null;
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
    public Collection<Class<?>> register(final Class<?> callback) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<?>, Collection<Class<?>>> register(final Class<?> callback,
        final Class<?>... callbacks) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Class<?>> register(final Object callback) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<?>, Collection<Class<?>>> register(final Object callback,
        final Object... callbacks) {

        // TODO Auto-generated method stub
        return null;
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
    public void setAnnotations(final Annotation[] annotations) {

        // TODO Auto-generated method stub

    }

    @Override
    public void setMethod(final ResourceMethodInvoker method) {

        // TODO Auto-generated method stub

    }

    @Override
    public void setResponseFilters(final ContainerResponseFilter[] responseFilters) {

        // TODO Auto-generated method stub

    }

    @Override
    public boolean setTimeout(final long time,
        final TimeUnit unit) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setTimeoutHandler(final TimeoutHandler handler) {

        // TODO Auto-generated method stub

    }

    @Override
    public void setWriterInterceptors(final WriterInterceptor[] writerInterceptors) {

        // TODO Auto-generated method stub

    }

}
