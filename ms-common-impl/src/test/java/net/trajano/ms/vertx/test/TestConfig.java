package net.trajano.ms.vertx.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import io.vertx.core.http.HttpServerOptions;

@Configuration

public class TestConfig {

    @Bean
    public URI baseUri(@Qualifier("server.port") final int httpPort) {

        return URI.create("http://localhost:" + httpPort);
    }

    @Bean
    public ObjectMapper objectMapper() {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JaxbAnnotationModule());
        return objectMapper;
    }

    @Bean
    @Primary
    public HttpServerOptions serverOptions(@Qualifier("server.port") final int httpPort) {

        return new HttpServerOptions()
            .setPort(httpPort);
    }

    @Bean
    @Qualifier("server.port")
    public int serverPort() throws IOException {

        try (final ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }
}
