package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import io.vertx.core.http.HttpClientRequest;
import net.trajano.ms.engine.internal.VertxBlockingInputStream;

public class VertxClientResponse extends ClientResponse {

    private final VertxBlockingInputStream is;

    public VertxClientResponse(final ClientConfiguration configuration,
        final HttpClientRequest httpClientRequest) {

        super(configuration);
        is = new VertxBlockingInputStream();

        httpClientRequest.handler(httpClientResponse -> {

            httpClientResponse.handler(buffer -> is.populate(buffer))
                .endHandler(aVoid -> is.end());
        }).exceptionHandler(e -> {
        });

    }

    @Override
    protected InputStream getInputStream() {

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
