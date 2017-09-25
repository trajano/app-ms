package net.trajano.ms.engine.internal;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestScope;

@Configuration
public class SpringConfiguration {

    //    @Bean
    //    public CustomScopeConfigurer customScopeConfigurer() {
    //
    //        final CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
    //        customScopeConfigurer.setScopes(Collections.singletonMap("request", org.springframework.web.context.request.RequestScope.class));
    //        return customScopeConfigurer;
    //
    //    }

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer(final RequestScope requestScope) {

        final CustomScopeConfigurer configurer = new CustomScopeConfigurer();

        configurer.setScopes(Collections.singletonMap("request", requestScope));
        return configurer;
    }

    @Bean
    public static RequestScope requestScope() {

        return new RequestScope();
    }

    @PostConstruct
    public void init() {

        System.out.println("FFF");
    }
}
