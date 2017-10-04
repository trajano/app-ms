package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import net.trajano.ms.engine.internal.Conversions;
import net.trajano.ms.engine.internal.VertxBlockingInputStream;
import net.trajano.ms.engine.internal.VertxOutputStream;

public class VertxClientEngine implements
    ClientHttpEngine {

    private final HttpClient httpClient;

    private final SSLContext sslContext;

    public VertxClientEngine(final HttpClient httpClient) {

        this.httpClient = httpClient;
        try {
            sslContext = SSLContext.getDefault();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

        httpClient.close();

    }

    @Override
    public HostnameVerifier getHostnameVerifier() {

        return null;
    }

    @Override
    public SSLContext getSslContext() {

        return sslContext;
    }

    @Override
    public ClientResponse invoke(final ClientInvocation request) {

        final RequestOptions options = Conversions.toRequestOptions(request.getUri());
        final HttpClientRequest clientRequest = httpClient.request(HttpMethod.valueOf(request.getMethod()), options);

        final Future<ClientResponse> futureClientResponse = Future.future();

        clientRequest.handler(clientResponse -> {

            final ClientResponse response = new ClientResponse(request.getClientConfiguration()) {

                private final VertxBlockingInputStream is = new VertxBlockingInputStream();

                @Override
                protected InputStream getInputStream() {

                    clientResponse.handler(buffer -> is.populate(buffer))
                        .endHandler(aVoid -> is.end());

                    return is;
                }

                @Override
                public void releaseConnection() throws IOException {

                    is.close();
                    clientRequest.end();

                }

                @Override
                protected void setInputStream(final InputStream is) {

                    throw new UnsupportedOperationException("Cannot set input stream");
                }
            };
            futureClientResponse.complete(response);

        });
        request.getHeaders().asMap().forEach((name,
            value) -> {
            clientRequest.putHeader(name, value);
        });

        try {
            request.writeRequestBody(new VertxOutputStream(clientRequest));
            clientRequest.end();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return futureClientResponse.result();
    }

}
