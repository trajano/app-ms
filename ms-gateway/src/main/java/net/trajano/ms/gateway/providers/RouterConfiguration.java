package net.trajano.ms.gateway.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * Provides the router which will act as the gateway.
 *
 * @author Archimedes Trajano
 */
@Configuration
public class RouterConfiguration {

    @Autowired
    private ConfigurableEnvironment env;

    @Bean
    public Router router(final Vertx vertx) {

        final Router router = Router.router(vertx);

        env.getPropertySources().forEach(x -> System.out.println(x));

        return router;
    }

}
