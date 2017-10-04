package net.trajano.ms.engine.internal;

import java.util.Collections;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestScope;

@Configuration
public class SpringConfiguration {

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer(final RequestScope scope) {

        final CustomScopeConfigurer configurer = new CustomScopeConfigurer();

        configurer.setScopes(Collections.singletonMap("request", scope));
        return configurer;
    }

    @Bean
    public static RequestScope requestScope() {

        return new RequestScope();
    }

}
