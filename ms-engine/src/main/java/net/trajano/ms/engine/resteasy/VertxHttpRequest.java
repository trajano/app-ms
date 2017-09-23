package net.trajano.ms.engine.resteasy;

import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.plugins.server.BaseHttpRequest;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyUriInfo;

import io.vertx.core.http.HttpServerRequest;
import net.trajano.ms.engine.internal.VertxBlockingInputStream;

public class VertxHttpRequest extends BaseHttpRequest implements
    HttpRequest {

    private final HttpHeaders headers;

    private final HttpServerRequest vertxRequest;

    public VertxHttpRequest(final URI baseUri,
        final HttpServerRequest vertxRequest) {

        super(new ResteasyUriInfo(baseUri, URI.create(vertxRequest.absoluteURI())));
        this.vertxRequest = vertxRequest;

        // TODO implement passthrough to vertxrequest instead.
        final MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        vertxRequest.headers().forEach(t -> headerMap.add(t.getKey(), t.getValue()));
        headers = new ResteasyHttpHeaders(headerMap);

    }

    @Override
    public void forward(final String path) {

        // TODO Auto-generated method stub

    }

    @Override
    public ResteasyAsynchronousContext getAsyncContext() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getAttribute(final String attribute) {

        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getAttributeNames() {

        throw new UnsupportedOperationException();
    }

    @Override
    public HttpHeaders getHttpHeaders() {

        return headers;
    }

    @Override
    public String getHttpMethod() {

        return vertxRequest.method().name();
    }

    @Override
    public InputStream getInputStream() {

        return new VertxBlockingInputStream(vertxRequest);
    }

    @Override
    public MultivaluedMap<String, String> getMutableHeaders() {

        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(final String name) {

        throw new UnsupportedOperationException();

    }

    @Override
    public void setAttribute(final String name,
        final Object value) {

        throw new UnsupportedOperationException();

    }

    @Override
    public void setHttpMethod(final String method) {

        throw new UnsupportedOperationException();

    }

    @Override
    public void setInputStream(final InputStream stream) {

        throw new UnsupportedOperationException();

    }

    @Override
    public boolean wasForwarded() {

        return false;
    }

}
