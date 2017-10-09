package net.trajano.ms.gateway.providers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.client.WebClient;

/**
 * Provides Vertx objects.
 *
 * @author Archimedes Trajano
 */
@Configuration
public class VertxBeanProvider {

    @Bean
    public HttpClient httpClient(final Vertx vertx,
        final HttpClientOptions httpClientOptions) {

        return vertx.createHttpClient(httpClientOptions);
    }

    @Bean
    public HttpServer httpServer(final Vertx vertx,
        final HttpServerOptions httpServerOptions) {

        return vertx.createHttpServer(httpServerOptions);
    }

    @Bean
    public Vertx vertx(final VertxOptions vertxOptions) {

        return Vertx.vertx(vertxOptions);
    }

    @Bean
    public WebClient webClient(final Vertx vertx,
        final HttpClient httpClient) {

        return WebClient.wrap(httpClient);
    }

}
