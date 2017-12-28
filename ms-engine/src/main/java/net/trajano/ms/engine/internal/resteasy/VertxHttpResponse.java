package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.HttpResponse;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.Conversions;
import net.trajano.ms.engine.internal.VertxOutputStream;

public class VertxHttpResponse implements
    HttpResponse {

    private final RoutingContext context;

    private OutputStream os;

    private final MultivaluedMap<String, Object> outputHeaders;

    private final HttpServerResponse vertxResponse;

    public VertxHttpResponse(final RoutingContext context) {

        this.context = context;
        vertxResponse = context.response();
        os = new VertxOutputStream(vertxResponse);
        outputHeaders = new VertxOutputHeaders(vertxResponse);
    }

    @Override
    public void addNewCookie(final NewCookie cookie) {

        context.addCookie(Conversions.toVertxCookie(cookie));

    }

    @Override
    public void flushBuffer() throws IOException {

        os.flush();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultivaluedMap<String, Object> getOutputHeaders() {

        return outputHeaders;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {

        return os;
    }

    @Override
    public int getStatus() {

        return vertxResponse.getStatusCode();
    }

    @Override
    public boolean isCommitted() {

        return vertxResponse.headWritten();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {

        if (isCommitted()) {
            throw new IllegalStateException("Response is committed");
        }
        setStatus(200);
        vertxResponse.headers().clear();
    }

    @Override
    public void sendError(final int status) throws IOException {

        sendError(status, Status.fromStatusCode(status).getReasonPhrase());

    }

    @Override
    public void sendError(final int status,
        final String message) throws IOException {

        vertxResponse.setStatusCode(status);
        vertxResponse.setStatusMessage(message);

    }

    /**
     * The output stream cannot be set. {@inheritDoc}
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void setOutputStream(final OutputStream os) {

        this.os = os;
    }

    @Override
    public void setStatus(final int status) {

        vertxResponse.setStatusCode(status);
        vertxResponse.setStatusMessage(Status.fromStatusCode(status).getReasonPhrase());

    }

}
