package net.trajano.ms.gateway.providers;

import static net.trajano.ms.gateway.internal.MediaTypes.APPLICATION_JSON;

import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import io.vertx.ext.web.Router;
import net.trajano.ms.gateway.handlers.SelfRegisteringRoutingContextHandler;
import net.trajano.ms.gateway.internal.ContextSettingHandler;

/**
 * Configures the routes on the gateway.
 *
 * @author Archimedes Trajano
 */
@Component
public class Routes {

    public static final String CONTEXT_PROTECTED = "protected";

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Routes.class);

    @Value("${http.defaultBodyLimit:-1}")
    private long defaultBodyLimit;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private List<SelfRegisteringRoutingContextHandler> handlers;

    /**
     * Path that renders the JWKS used by the gateway.
     */
    @Value("${jwks.path}")
    private String jwksPath;

    /**
     * Source JWKS URI used by the gateway.
     */
    @Value("${jwks.source}")
    private URI jwksSourceURI;

    @Autowired
    private Router router;

    @PostConstruct
    public void registerHandlers() {
        // Register the context setting handlers

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
                        .handler(new ContextSettingHandler(true, from, to, limit));
                } else {
                    LOG.info("get JSON from={} to={}, unprotected, exact={}", from, to, exact);
                    router.get(from + wildcard)
                        .produces(APPLICATION_JSON)
                        .handler(new ContextSettingHandler(false, from, to, limit));
                }
            } else {
                if (protectedRoute) {
                    LOG.info("route from={} to={}, protected, exact={}", from, to, exact);
                    router.route(from + wildcard)
                        .handler(new ContextSettingHandler(true, from, to, limit));
                } else {
                    LOG.info("route from={} to={}, unprotected, exact={}", from, to, exact);
                    router.route(from + wildcard)
                        .handler(new ContextSettingHandler(false, from, to, limit));
                }
            }
            ++i;
        }

        router.get(jwksPath)
            .produces(APPLICATION_JSON)
            .handler(new ContextSettingHandler(false, jwksPath, jwksSourceURI, defaultBodyLimit));

        // Register the actual handlers
        handlers.forEach(h -> {
            LOG.debug("Registering {} #{} to {}", h, h.getClass().getAnnotation(Order.class).value(), router);
            h.register(router);
        });
    }

}
