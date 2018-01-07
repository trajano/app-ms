package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientRequest;
import net.trajano.ms.engine.internal.Conversions;
import net.trajano.ms.engine.internal.VertxBlockingInputStream;

public class VertxClientResponse extends ClientResponse {

    private static final Logger LOG = LoggerFactory.getLogger(VertxClientResponse.class);

    private Throwable exception;

    private final VertxBlockingInputStream is;

    private final Semaphore metadataLock = new Semaphore(0);

    public VertxClientResponse(final ClientConfiguration configuration,
        final HttpClientRequest httpClientRequest) {

        super(configuration);
        is = new VertxBlockingInputStream();

        httpClientRequest.handler(httpClientResponse -> {
            LOG.debug("Status = {}", httpClientResponse.statusCode());
            setStatus(httpClientResponse.statusCode());
            final MultiMap headers = httpClientResponse.headers();
            setHeaders(Conversions.toMultivaluedStringMap(headers));
            httpClientResponse.handler(is::populate)
                .endHandler(aVoid -> is.end());
            LOG.trace("prepared HTTP client response handler");
            metadataLock.release();
        }).exceptionHandler(e -> {
            LOG.error("exception handling response", e);
            is.error(e);
            exception = e;
            metadataLock.release();
        });
        LOG.trace("prepared HTTP client request handler");

    }

    @Override
    public Map<String, NewCookie> getCookies() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.debug("attempting to get cookies, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        final Map<String, NewCookie> m = super.getCookies();
        metadataLock.release();
        return m;
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.debug("attempting to get headers, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        final MultivaluedMap<String, Object> m = super.getHeaders();
        metadataLock.release();
        return m;
    }

    @Override
    protected InputStream getInputStream() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.trace("inputStream={}", is);
        return is;
    }

    @Override
    public Locale getLanguage() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.debug("attempting to get cookies, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        final Locale m = super.getLanguage();
        metadataLock.release();
        return m;
    }

    @Override
    public MediaType getMediaType() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.debug("attempting to get media type, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        final MediaType m = super.getMediaType();
        metadataLock.release();
        return m;
    }

    @Override
    public int getStatus() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.debug("attempting to get media type, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        final int m = super.getStatus();
        metadataLock.release();
        return m;
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.debug("attempting to get string headers, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        final MultivaluedMap<String, String> m = super.getStringHeaders();
        metadataLock.release();
        return m;
    }

    @Override
    public boolean hasEntity() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.debug("attempting to check entity, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        abortIfClosed();
        final boolean m = entity != null || super.getMediaType() != null;
        metadataLock.release();
        return m;

    }

    @Override
    public void releaseConnection() throws IOException {

        LOG.debug("connection released");

    }

    @Override
    protected void setInputStream(final InputStream is) {

        throw new UnsupportedOperationException();

    }

}
