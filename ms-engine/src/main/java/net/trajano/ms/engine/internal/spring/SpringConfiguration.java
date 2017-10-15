package net.trajano.ms.engine.internal.spring;

import java.util.Collections;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
