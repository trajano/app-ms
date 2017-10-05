package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import net.trajano.ms.engine.internal.Conversions;
import net.trajano.ms.engine.internal.VertxOutputStream;

public class VertxClientEngine implements
    ClientHttpEngine {

    private final SSLContext sslContext;

    private final Vertx vertx;

    public VertxClientEngine(final Vertx vertx) {

        this.vertx = vertx;
        try {
            sslContext = SSLContext.getDefault();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

        System.out.println("closing");

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

        final HttpClient httpClient = vertx.createHttpClient();
        final RequestOptions options = Conversions.toRequestOptions(request.getUri());
        final HttpClientRequest httpClientRequest = httpClient.request(HttpMethod.valueOf(request.getMethod()), options);

        final VertxClientResponse clientResponse = new VertxClientResponse(request.getClientConfiguration(), httpClientRequest);

        request.getHeaders().asMap().forEach((name,
            value) -> {
            httpClientRequest.putHeader(name, value);
        });

        try {
            request.writeRequestBody(new VertxOutputStream(httpClientRequest));
            System.out.println("wrote request body");
            return clientResponse;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            httpClientRequest.end();
            httpClient.close();
        }
    }

}
