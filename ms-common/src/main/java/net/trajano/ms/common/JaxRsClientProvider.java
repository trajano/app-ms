package net.trajano.ms.common;

import javax.ws.rs.client.ClientBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Configuration
public class JaxRsClientProvider {

    @Bean
    public ClientBuilder clientBuilder() {

        return ClientBuilder.newBuilder().register(JacksonJaxbJsonProvider.class);
    }
}
