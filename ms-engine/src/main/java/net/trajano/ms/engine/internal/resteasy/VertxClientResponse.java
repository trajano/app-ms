package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpClientRequest;
import net.trajano.ms.engine.internal.VertxBlockingInputStream;

public class VertxClientResponse extends ClientResponse {

    private static final Logger LOG = LoggerFactory.getLogger(VertxClientResponse.class);

    private final VertxBlockingInputStream is;

    public VertxClientResponse(final ClientConfiguration configuration,
        final HttpClientRequest httpClientRequest) {

        super(configuration);
        is = new VertxBlockingInputStream();

        httpClientRequest.handler(httpClientResponse -> {

            httpClientResponse.handler(buffer -> is.populate(buffer))
                .endHandler(aVoid -> is.end());
            LOG.trace("prepared HTTP client response handler");
        }).exceptionHandler(e -> {
            is.error(e);
        });
        LOG.trace("prepared HTTP client request handler");

    }

    @Override
    protected InputStream getInputStream() {

        LOG.trace("inputStream={}", is);
        return is;
    }

    @Override
    public void releaseConnection() throws IOException {

        //httpClientRequest.end();

    }

    @Override
    protected void setInputStream(final InputStream is) {

        throw new UnsupportedOperationException();

    }

}
