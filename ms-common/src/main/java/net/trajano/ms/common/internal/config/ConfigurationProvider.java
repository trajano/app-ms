package net.trajano.ms.common.internal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;

@Configuration
public class ConfigurationProvider {

    @Value("${http.client.proxy.host:#{null}}")
    private String httpClientProxyHost;

    @Value("${http.client.proxy.password:#{null}}")
    private String httpClientProxyPassword;

    /**
     * Uses the default value from {@link ProxyOptions#DEFAULT_PORT}
     */
    @Value("${http.client.proxy.port:3128}")
    private int httpClientProxyPort;

    /**
     * Uses the default value from {@link ProxyOptions#DEFAULT_TYPE}
     */
    @Value("${http.client.proxy.proxyType:HTTP}")
    private ProxyType httpClientProxyType;

    @Value("${http.client.proxy.username:#{null}}")
    private String httpClientProxyUsername;

    @Value("${http.port:8900}")
    private int httpPort;

    @Value("${vertx.warningExceptionTime:1}")
    private long vertxWarningExceptionTime;

    @Value("${vertx.workerPoolSize:50}")
    private int vertxWorkerPoolSize;

    @Bean
    public HttpClientOptions httpClientOptions() {

        final HttpClientOptions options = new HttpClientOptions()
            .setPipelining(true);
        if (httpClientProxyHost != null) {
            final ProxyOptions proxyOptions = new ProxyOptions()
                .setHost(httpClientProxyHost)
                .setPort(httpClientProxyPort)
                .setType(httpClientProxyType)
                .setUsername(httpClientProxyUsername)
                .setPassword(httpClientProxyPassword);
            options.setProxyOptions(proxyOptions);
        }
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
