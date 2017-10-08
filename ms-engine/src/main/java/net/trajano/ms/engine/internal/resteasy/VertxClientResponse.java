package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            setStatus(httpClientResponse.statusCode());
            setHeaders(Conversions.toMultivaluedStringMap(httpClientResponse.headers()));
            httpClientResponse.handler(buffer -> is.populate(buffer))
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
    protected InputStream getInputStream() {

        if (exception != null) {
            throw new IllegalStateException(exception);
        }
        LOG.trace("inputStream={}", is);
        return is;
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
    public void releaseConnection() throws IOException {

        LOG.debug("connection released");

    }

    @Override
    protected void setInputStream(final InputStream is) {

        throw new UnsupportedOperationException();

    }

}
