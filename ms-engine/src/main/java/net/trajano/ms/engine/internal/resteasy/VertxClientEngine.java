package net.trajano.ms.engine.internal.resteasy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import net.trajano.ms.engine.internal.Conversions;
import net.trajano.ms.engine.internal.VertxOutputStream;

public class VertxClientEngine implements
    ClientHttpEngine {

    private static final Logger LOG = LoggerFactory.getLogger(VertxClientEngine.class);

    private final HostnameVerifier hostnameVerifier;

    private final HttpClient httpClient;

    private final SSLContext sslContext;

    public VertxClientEngine(final HttpClient httpClient) {

        try {
            this.httpClient = httpClient;
            sslContext = SSLContext.getDefault();
            hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        } catch (final NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void close() {

        LOG.trace("closing {}", this);

    }

    @Override
    public HostnameVerifier getHostnameVerifier() {

        return hostnameVerifier;
    }

    @Override
    public SSLContext getSslContext() {

        return sslContext;
    }

    @Override
    public ClientResponse invoke(final ClientInvocation request) {

        final RequestOptions options = Conversions.toRequestOptions(request.getUri());
        final HttpClientRequest httpClientRequest = httpClient.request(HttpMethod.valueOf(request.getMethod()), options);

        final VertxClientResponse clientResponse = new VertxClientResponse(request.getClientConfiguration(), httpClientRequest);

        request.getHeaders().asMap().forEach((name,
            value) -> httpClientRequest.putHeader(name, value));

        try (final VertxOutputStream os = new VertxOutputStream(httpClientRequest)) {
            request.writeRequestBody(os);
            LOG.trace("request body written on {}", this);
            return clientResponse;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            httpClientRequest.end();
        }
    }

}
