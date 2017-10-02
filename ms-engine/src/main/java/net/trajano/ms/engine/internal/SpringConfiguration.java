package net.trajano.ms.engine.internal;

import java.util.Collections;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

@Configuration
public class SpringConfiguration {

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {

        final CustomScopeConfigurer configurer = new CustomScopeConfigurer();

        configurer.setScopes(Collections.singletonMap("request", new SimpleThreadScope()));
        return configurer;
    }

}
