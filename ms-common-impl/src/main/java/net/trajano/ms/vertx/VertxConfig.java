package net.trajano.ms.vertx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import net.trajano.ms.engine.internal.spring.SpringConfiguration;
import net.trajano.ms.engine.jaxrs.JaxRsRouter;

/**
 * Vert.X Microservice Engine Configuration.
 *
 * @author Archimedes Trajano
 */
@Configuration
@ComponentScan
@ComponentScan(basePackageClasses = {
    SpringConfiguration.class,
    JaxRsRouter.class
})
public class VertxConfig {

    /**
     * Idle timeout, in seconds. zero means don't timeout. This determines if a
     * connection will timeout and be closed if no data is received within the
     * timeout. This defaults to 60 seconds.
     */
    @Value("${http.client.idle_timeout:60}")
    private int httpClientIdleTimeout;

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

    /**
     * DNS Address resolver options where the max queries is increased to 10 to
     * support more environments.
     *
     * @return address resolver options
     */
    @Bean
    public AddressResolverOptions addressResolverOptions() {

        return new AddressResolverOptions()
            .setMaxQueries(10);
    }

    @Bean
    public HttpClientOptions httpClientOptions() {

        final HttpClientOptions options = new HttpClientOptions()
            .setIdleTimeout(httpClientIdleTimeout)
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

        return new HttpServerOptions()
            .setPort(httpPort);
    }

    @Bean
    public VertxOptions vertxOptions(final AddressResolverOptions addressResolverOptions) {

        return new VertxOptions()
            .setAddressResolverOptions(addressResolverOptions)
            .setWarningExceptionTime(vertxWarningExceptionTime)
            .setWorkerPoolSize(vertxWorkerPoolSize);
    }

}
