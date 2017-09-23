package net.trajano.ms.engine;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.jboss.resteasy.spi.HttpResponse;

import io.vertx.core.http.HttpServerResponse;
import net.trajano.ms.engine.internal.VertxOutputStream;

public class VertxHttpResponse implements
    HttpResponse {

    private final MultivaluedMap<String, Object> outputHeaders;

    private final HttpServerResponse vertxResponse;

    public VertxHttpResponse(final HttpServerResponse vertxResponse) {

        this.vertxResponse = vertxResponse;
        outputHeaders = new MultivaluedHashMap<>();
    }

    @Override
    public void addNewCookie(final NewCookie cookie) {

        outputHeaders.add(javax.ws.rs.core.HttpHeaders.SET_COOKIE, cookie);
    }

    @Override
    public MultivaluedMap<String, Object> getOutputHeaders() {

        return outputHeaders;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {

        return new VertxOutputStream(vertxResponse);
    }

    @Override
    public int getStatus() {

        return vertxResponse.getStatusCode();
    }

    @Override
    public boolean isCommitted() {

        return vertxResponse.headWritten();
    }

    @Override
    public void reset() {

        vertxResponse.reset();
    }

    @Override
    public void sendError(final int status) throws IOException {

        vertxResponse.setStatusCode(status);

    }

    @Override
    public void sendError(final int status,
        final String message) throws IOException {

        vertxResponse.setStatusCode(status);
        vertxResponse.setStatusMessage(message);

    }

    @Override
    public void setOutputStream(final OutputStream os) {

        throw new UnsupportedOperationException();

    }

    @Override
    public void setStatus(final int status) {

        vertxResponse.setStatusCode(status);

    }

}
