package net.trajano.ms.gateway.providers;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger LOG = LoggerFactory.getLogger(RouterConfiguration.class);

    @Value("${http.defaultBodyLimit:-1}")
    private long defaultBodyLimit;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private Handlers handlers;

    @Value("${authorization.refreshTokenPath:/refresh}")
    private String refreshTokenPath;

    @Bean
    public Router router(final Vertx vertx) {

        final Router router = Router.router(vertx);

        router.post(refreshTokenPath)
            .consumes("application/x-www-form-urlencoded")
            .produces("application/json")
            .handler(handlers.refreshHandler());

        int i = 0;
        while (env.containsProperty(String.format("routes[%d].from", i))) {

            final String from = env.getProperty(String.format("routes[%d].from", i));
            final URI to = env.getProperty(String.format("routes[%d].to", i), URI.class);
            final boolean protectedRoute = env.getProperty(String.format("routes[%d].protected", i), Boolean.class, true);
            final long limit = env.getProperty(String.format("routes[%d].limit", i), Long.class, defaultBodyLimit);
            final boolean exact = env.getProperty(String.format("routes[%d].protected", i), Boolean.class, false);

            String wildcard = "/*";
            if (exact) {
                wildcard = "";
            }
            if (protectedRoute) {
                LOG.info("route from={} to={}, protected, exact={}", from, to, exact);
                router.route(from + wildcard)
                    .handler(handlers.protectedHandler(from, to))
                    .failureHandler(handlers.failureHandler());
            } else {
                LOG.info("route from={} to={}, unprotected, exact={}", from, to, exact);
                router.route(from + wildcard)
                    .handler(handlers.unprotectedHandler(from, to))
                    .failureHandler(handlers.failureHandler());
            }
            ++i;
        }

        return router;
    }

}
