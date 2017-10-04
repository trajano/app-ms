package net.trajano.ms.common.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;

@Configuration
public class ConfigurationProvider {

    @Value("${http.port:8900}")
    private int httpPort;

    @Value("${vertx.warningExceptionTime:1}")
    private long vertxWarningExceptionTime;

    @Value("${vertx.workerPoolSize:50}")
    private int vertxWorkerPoolSize;

    @Bean
    public HttpServerOptions httpServerOptions() {

        final HttpServerOptions options = new HttpServerOptions();
        options.setPort(httpPort);
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
