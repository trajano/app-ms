package net.trajano.ms.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;

@Configuration
public class ConfigurationProvider {

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

        final HttpClientOptions options = new HttpClientOptions();
        return options;
    }

    @Bean
    public HttpServerOptions httpServerOptions() {

        final HttpServerOptions options = new HttpServerOptions()
            .setPort(httpPort);
        return options;
    }

    @Bean
    public VertxOptions vertxOptions() {

        final VertxOptions options = new VertxOptions();
        options.setWarningExceptionTime(vertxWarningExceptionTime);
        options.setWorkerPoolSize(vertxWorkerPoolSize);

        return options;
    }

}
