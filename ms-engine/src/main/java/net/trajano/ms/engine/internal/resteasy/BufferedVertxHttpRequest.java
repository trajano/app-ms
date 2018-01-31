package net.trajano.ms.engine.internal.resteasy;

import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.plugins.server.BaseHttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.NullInputStream;
import net.trajano.ms.engine.internal.VertxBufferInputStream;
import net.trajano.ms.engine.internal.VertxRoutingContextHttpHeaders;

public class BufferedVertxHttpRequest extends BaseHttpRequest {

    private static final Logger LOG = LoggerFactory.getLogger(VertxHttpRequest.class);

    /**
     * Asynchronous context.
     */
    private final ResteasyAsynchronousContext asynchronousContext;

    /**
     * Vert.X routing context
     */
    private final RoutingContext context;

    private boolean forwarded;

    private final HttpHeaders httpHeaders;

    private Buffer requestBuffer;

    private final HttpServerRequest vertxRequest;

    public BufferedVertxHttpRequest(final RoutingContext context,
        final ResteasyUriInfo uriInfo,
        final ResteasyProviderFactory providerFactory) {

        super(uriInfo);

        this.context = context;
        vertxRequest = context.request();

        httpHeaders = new VertxRoutingContextHttpHeaders(context);

        LOG.debug("vertxRequest.isEnded={}", vertxRequest.isEnded());

        asynchronousContext = new VertxExecutionContext(context, providerFactory, this);
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

        return context.get(attribute);
    }

    @Override
    public Enumeration<String> getAttributeNames() {

        return Collections.enumeration(context.data().keySet());
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

        if (!vertxRequest.isEnded()) {
            if (requestBuffer == null) {
                throw new IllegalStateException("getInputStream before setInputStream");
            }
            return new VertxBufferInputStream(requestBuffer);
        } else {
            return NullInputStream.nullInputStream();
        }
    }

    @Override
    public MultivaluedMap<String, String> getMutableHeaders() {

        throw new UnsupportedOperationException("Mutable headers are not supported");
    }

    @Override
    public void removeAttribute(final String name) {

        context.remove(name);

    }

    public void requestBuffer(final Buffer _requestBuffer) {

        requestBuffer = _requestBuffer;
    }

    @Override
    public void setAttribute(final String name,
        final Object value) {

        context.put(name, value);

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
