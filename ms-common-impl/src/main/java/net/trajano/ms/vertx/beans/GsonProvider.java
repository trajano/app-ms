package net.trajano.ms.vertx.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class GsonProvider {

    @Bean
    public Gson gson() {

        return new GsonBuilder().create();
    }
}
