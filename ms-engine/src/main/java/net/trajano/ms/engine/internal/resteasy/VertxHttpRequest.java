package net.trajano.ms.engine.internal.resteasy;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.BaseHttpRequest;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyUriInfo;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.Conversions;
import net.trajano.ms.engine.internal.VertxBlockingInputStream;

/**
 * For the most part the request is prebuilt and immutable. That way there is no
 * need for most of the setters.
 *
 * @author Archimedes Trajano
 */
public class VertxHttpRequest extends BaseHttpRequest {

    private final ResteasyAsynchronousContext asynchronousContext;

    private final Map<String, Object> attributes;

    private boolean forwarded;

    private final ResteasyHttpHeaders httpHeaders;

    private final VertxBlockingInputStream is;

    private final HttpServerRequest vertxRequest;

    public VertxHttpRequest(final RoutingContext context,
        final URI baseUri,
        final SynchronousDispatcher dispatcher) {

        super(new ResteasyUriInfo(context.normalisedPath(), context.request().query(), baseUri.toString()));

        vertxRequest = context.request();
        httpHeaders = new ResteasyHttpHeaders(Conversions.toMultivaluedStringMap(vertxRequest.headers()),
            Conversions.toCookies(context.cookies()));
        attributes = new HashMap<>();

        if (!vertxRequest.isEnded()) {
            is = new VertxBlockingInputStream();
            vertxRequest
                .handler(buffer -> is.populate(buffer))
                .endHandler(aVoid -> is.end());
        } else {
            is = null;
        }

        final HttpResponse response = new VertxHttpResponse(context);

        asynchronousContext = new VertxExecutionContext(context, dispatcher, this, response);
    }

    @Override
    public void forward(final String path) {

        throw new UnsupportedOperationException();
    }

    @Override
    public ResteasyAsynchronousContext getAsyncContext() {

        return asynchronousContext;
    }

    @Override
    public Object getAttribute(final String attribute) {

        return attributes.get(attribute);
    }

    @Override
    public Enumeration<String> getAttributeNames() {

        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public HttpHeaders getHttpHeaders() {

        return httpHeaders;
    }

    @Override
    public String getHttpMethod() {

        return vertxRequest.method().name();
    }

    @Override
    public InputStream getInputStream() {

        return is;
    }

    @Override
    public MultivaluedMap<String, String> getMutableHeaders() {

        return httpHeaders.getMutableHeaders();
    }

    @Override
    public void removeAttribute(final String name) {

        attributes.remove(name);

    }

    @Override
    public void setAttribute(final String name,
        final Object value) {

        attributes.put(name, value);

    }

    @Override
    public void setHttpMethod(final String method) {

        throw new UnsupportedOperationException();
    }

    @Override
    public void setInputStream(final InputStream inputStream) {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean wasForwarded() {

        return forwarded;
    }
}
