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
            System.out.println("prepared client response handlers");
        }).exceptionHandler(e -> {
            httpClientRequest.connection().close();
            throw new RuntimeException("innie", e);
        });
        System.out.println("prepared response handlers");

    }

    @Override
    protected InputStream getInputStream() {

        System.out.println("inputstream get");
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
