package net.trajano.ms.gateway.providers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;

@Configuration
public class ConfigurationProvider {

    /**
     * Idle timeout, in seconds. zero means don't timeout. This determines if a
     * connection will timeout and be closed if no data is received within the
     * timeout. This defaults to 60 seconds.
     */
    @Value("${http.client.idle_timeout:60}")
    private int httpClientIdleTimeout;

    @Value("${http.client.max_pool_size:50}")
    private int httpClientMaxPoolSize;

    @Value("${http.client.proxy.host:#{null}}")
    private String httpClientProxyHost;

    @Value("${http.client.proxy.password:#{null}}")
    private String httpClientProxyPassword;

    @Value("${http.port:8910}")
    private int httpPort;

    @Value("${vertx.warningExceptionTime:1}")
    private long vertxWarningExceptionTime;

    @Value("${vertx.workerPoolSize:50}")
    private int vertxWorkerPoolSize;

    @Bean
    public HttpClientOptions httpClientOptions() {

        return new HttpClientOptions()
            .setIdleTimeout(httpClientIdleTimeout)
            .setPipelining(true)
            .setMaxPoolSize(httpClientMaxPoolSize);
    }

    @Bean
    public HttpServerOptions httpServerOptions() {

        return new HttpServerOptions()
            .setCompressionSupported(true)
            .setPort(httpPort);
    }

    @Bean
    public VertxOptions vertxOptions() {

        return new VertxOptions()
            .setWarningExceptionTime(vertxWarningExceptionTime)
            .setWorkerPoolSize(vertxWorkerPoolSize);
    }

}
