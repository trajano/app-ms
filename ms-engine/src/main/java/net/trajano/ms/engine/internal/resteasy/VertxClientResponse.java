package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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

    final SemaphoredHeaders<Object> semaphoredHeaders;

    @SuppressWarnings({
        "unchecked",
        "rawtypes"
    })
    public VertxClientResponse(final ClientConfiguration configuration,
        final HttpClientRequest httpClientRequest) {

        super(configuration);
        semaphoredHeaders = new SemaphoredHeaders<>();

        // Used in the base classes
        metadata = semaphoredHeaders;
        is = new VertxBlockingInputStream();

        httpClientRequest.handler(httpClientResponse -> {
            LOG.debug("Status = {}", httpClientResponse.statusCode());
            setStatus(httpClientResponse.statusCode());
            final MultiMap headers = httpClientResponse.headers();
            metadata.putAll((Map) Conversions.toMultivaluedStringMap(headers));
            httpClientResponse.handler(is::populate)
                .endHandler(aVoid -> is.end());
            LOG.trace("prepared HTTP client response handler");

            semaphoredHeaders.releaseLock();
        }).exceptionHandler(e -> {
            LOG.error("exception handling response", e);
            is.error(e);
            exception = e;
            semaphoredHeaders.releaseLock();
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
    public int getStatus() {

        semaphoredHeaders.acquireUninterruptibly();
        try {
            return super.getStatus();
        } finally {
            semaphoredHeaders.releaseLock();
        }
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
