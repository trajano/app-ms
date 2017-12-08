package net.trajano.ms.gateway.test;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.SocketUtils;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerOptions;

@Configuration
public class SpringTestConfiguration {

    @Bean
    @Primary
    public HttpServerOptions httpServerOptions() {

        return new HttpServerOptions()
            .setPort(SocketUtils.findAvailableTcpPort());

    }

    @Bean
    @Primary
    public HttpClient mockHttpClient() {

        return mock(HttpClient.class);
    }
}
