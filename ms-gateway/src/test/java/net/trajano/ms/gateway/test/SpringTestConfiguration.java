package net.trajano.ms.gateway.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.SocketUtils;

import io.vertx.core.http.HttpServerOptions;

@Configuration
public class SpringTestConfiguration {

    @Bean
    @Primary
    public HttpServerOptions httpServerOptions() {

        return new HttpServerOptions()
            .setPort(SocketUtils.findAvailableTcpPort());

    }
}
