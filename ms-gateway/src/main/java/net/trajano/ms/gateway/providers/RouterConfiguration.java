package net.trajano.ms.gateway.providers;

import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_FORM_URLENCODED;
import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_JSON;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

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

    @Value("${jwks.path}")
    private String jwksPath;

    @Value("${jwks.source}")
    private URI jwksSourceURI;

    @Autowired
    private RefreshHandler refreshHandler;

    @Value("${authorization.refresh_token_path:/refresh}")
    private String refreshTokenPath;

    @Autowired
    private RevocationHandler revocationHandler;

    @Value("${authorization.revocation_path:/logoff}")
    private String revocationPath;

    @Bean
    public Router router(final Vertx vertx) {

        final Router router = Router.router(vertx);

        router.route().handler(handlers.corsHandler());

        router.route().handler(CorsHandler.create(".+")
            .maxAgeSeconds(600)
            .allowedMethod(HttpMethod.GET)
            .allowedMethod(HttpMethod.POST)
            .allowedMethod(HttpMethod.PUT)
            .allowedMethod(HttpMethod.DELETE)
            .allowedMethod(HttpMethod.OPTIONS)
            .allowedHeader("Content-Type")
            .allowedHeader("Accept")
            .allowedHeader("Accept-Language")
            .allowedHeader("Authorization"));

        router.post(refreshTokenPath)
            .consumes(APPLICATION_FORM_URLENCODED)
            .produces(APPLICATION_JSON)
            .handler(BodyHandler.create().setBodyLimit(1024));
        router.post(refreshTokenPath)
            .consumes(APPLICATION_FORM_URLENCODED)
            .produces(APPLICATION_JSON)
            .handler(refreshHandler);

        router.post(revocationPath)
            .consumes(APPLICATION_FORM_URLENCODED)
            .produces(APPLICATION_JSON)
            .handler(BodyHandler.create().setBodyLimit(1024));
        router.post(revocationPath)
            .consumes(APPLICATION_FORM_URLENCODED)
            .produces(APPLICATION_JSON)
            .handler(revocationHandler);

        // Route JWKS path
        router.get(jwksPath)
            .produces(APPLICATION_JSON)
            .handler(handlers.unprotectedHandler(jwksPath, jwksSourceURI))
            .failureHandler(handlers.failureHandler());

        int i = 0;
        while (env.containsProperty(String.format("routes[%d].from", i))) {

            final String from = env.getProperty(String.format("routes[%d].from", i));
            final URI to = env.getProperty(String.format("routes[%d].to", i), URI.class);
            final boolean protectedRoute = env.getProperty(String.format("routes[%d].protected", i), Boolean.class, true);
            final long limit = env.getProperty(String.format("routes[%d].limit", i), Long.class, defaultBodyLimit);
            final boolean exact = env.getProperty(String.format("routes[%d].exact", i), Boolean.class, false);
            final boolean onlyGetJson = env.getProperty(String.format("routes[%d].onlyGetGson", i), Boolean.class, false);

            String wildcard = "/*";
            if (exact) {
                wildcard = "";
            }

            if (onlyGetJson) {
                // Special case when only JSON is wanted.  Primarily for reference requests.
                if (protectedRoute) {
                    LOG.info("get JSON from={} to={}, protected, exact={}", from, to, exact);
                    router.get(from + wildcard)
                        .produces(APPLICATION_JSON)
                        .handler(handlers.protectedHandler(from, to))
                        .failureHandler(handlers.failureHandler());
                } else {
                    LOG.info("get JSON from={} to={}, unprotected, exact={}", from, to, exact);
                    router.get(from + wildcard)
                        .produces(APPLICATION_JSON)
                        .handler(handlers.unprotectedHandler(from, to))
                        .failureHandler(handlers.failureHandler());
                }
            } else {
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
            }
            ++i;
        }
        router.route().failureHandler(handlers.failureHandler());

        return router;
    }

}
