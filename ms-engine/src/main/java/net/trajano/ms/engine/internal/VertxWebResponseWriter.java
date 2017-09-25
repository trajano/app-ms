package net.trajano.ms.engine.internal;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
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

        if (!response.ended()) {
            response.end();
        }

    }

    @Override
    public boolean enableResponseBuffering() {

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failure(final Throwable error) {

        response.setStatusCode(500).setStatusMessage(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).close();
        throw new RuntimeException(error);

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

        if (contentLength >= 0) {
            response.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        }
        response.setChunked(responseContext.isChunked());
        final StatusType status = responseContext.getStatusInfo();
        System.out.println(status.getStatusCode() + " " + status.getReasonPhrase());
        response.setStatusCode(status.getStatusCode());
        response.setStatusMessage(status.getReasonPhrase());
        responseContext.getStringHeaders().forEach((header,
            values) -> {
            response.putHeader(header, values);
        });
        return new VertxOutputStream(response);
    }

}
