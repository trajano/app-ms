package net.trajano.ms.engine.internal.spring;

import java.util.Collections;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import io.swagger.jackson.SwaggerModule;
import io.vertx.core.json.Json;

@Configuration
public class SpringConfiguration {

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer(final ContainerRequestScope scope) {

        final CustomScopeConfigurer configurer = new CustomScopeConfigurer();

        configurer.setScopes(Collections.singletonMap("request", scope));
        return configurer;
    }

    @Bean
    public static ContainerRequestScope requestScope() {

        return new ContainerRequestScope();
    }

    /**
     * This {@link ObjectMapper} sets up Jackson for some common defaults. This
     * utilizes Vert.x mapper to support their json objects in addition it will
     * prevent <code>null</code> from being put into a resulting JSON.
     *
     * @return configured object mapper
     */
    @Bean
    public ObjectMapper objectMapper() {

        return Json.mapper.copy()
            .registerModule(new SwaggerModule())
            .registerModule(new JaxbAnnotationModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

}
