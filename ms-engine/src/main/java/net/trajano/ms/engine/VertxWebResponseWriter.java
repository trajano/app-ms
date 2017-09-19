package net.trajano.ms.engine;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import io.vertx.core.http.HttpServerResponse;

public class VertxWebResponseWriter implements
    ContainerResponseWriter {

    private final HttpServerResponse response;

    public VertxWebResponseWriter(final HttpServerResponse response) {

        this.response = response;
    }

    @Override
    public void commit() {

        response.end();

    }

    @Override
    public boolean enableResponseBuffering() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failure(final Throwable error) {

        throw new UnsupportedOperationException();

    }

    @Override
    public void setSuspendTimeout(final long timeOut,
        final TimeUnit timeUnit) throws IllegalStateException {

        throw new UnsupportedOperationException();

    }

    @Override
    public boolean suspend(final long timeOut,
        final TimeUnit timeUnit,
        final TimeoutHandler timeoutHandler) {

        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream writeResponseStatusAndHeaders(final long contentLength,
        final ContainerResponse responseContext) throws ContainerException {

        final StatusType status = responseContext.getStatusInfo();
        response.setStatusCode(status.getStatusCode());
        response.setStatusMessage(status.getReasonPhrase());
        responseContext.getStringHeaders().forEach((header,
            values) -> {
            response.putHeader(header, values);
        });
        return new VertxOutputStream(response);
    }

}
