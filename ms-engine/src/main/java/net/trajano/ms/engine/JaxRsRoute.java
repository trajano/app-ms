package net.trajano.ms.engine;

import static java.util.Collections.singletonMap;

import java.net.URI;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.SpringConfiguration;
import net.trajano.ms.engine.internal.VertxBinder;
import net.trajano.ms.engine.internal.VertxBlockingInputStream;
import net.trajano.ms.engine.internal.VertxRequestContextFilter;
import net.trajano.ms.engine.internal.VertxSecurityContext;
import net.trajano.ms.engine.internal.VertxWebResponseWriter;

public class JaxRsRoute extends AbstractVerticle implements
    Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsRoute.class);

    /**
     * Constructs a new route for the given router to a JAX-RS application.
     *
     * @param router
     * @param applicationClass
     */
    public static void route(final Vertx vertx,
        final Router router,
        final Class<? extends Application> applicationClass) {

        new JaxRsRoute(vertx, router, applicationClass);

    }

    private ApplicationHandler appHandler;

    private final URI baseUri;

    private JaxRsRoute(
        final Vertx vertx,
        final Router router,
        final Class<? extends Application> applicationClass) {

        this.vertx = vertx;

        final ApplicationPath annotation = applicationClass.getAnnotation(ApplicationPath.class);
        if (annotation != null) {
            baseUri = URI.create(annotation.value() + "/").normalize();
        } else {
            baseUri = URI.create("/");
        }

        final String resourcePackage = applicationClass.getPackage().getName();

        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage(resourcePackage);
        beanConfig.setScan(true);
        beanConfig.setBasePath(baseUri.getPath());
        beanConfig.scanAndRead();

        try {
            final Swagger swagger = beanConfig.getSwagger();
            final String json = Json.mapper().writeValueAsString(swagger);
            router.get(baseUri.getPath()).produces("application/json").handler(context -> context.response().putHeader("Content-Type", "application/json").end(json));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final ResourceConfig resourceConfig = new ResourceConfig();
        final AnnotationConfigApplicationContext ctx;
        if (applicationClass.getAnnotation(Configuration.class) != null) {
            ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class, applicationClass);
        } else {
            ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        }

        //  resourceConfig.addProperties(singletonMap("contextConfig", applicationContext));
        resourceConfig.addProperties(singletonMap(ServerProperties.PROVIDER_PACKAGES, resourcePackage));

        resourceConfig.register(new VertxBinder(vertx, ctx));
        resourceConfig.register(new VertxRequestContextFilter());
        resourceConfig.register(JacksonJaxbJsonProvider.class);

        vertx.executeBlocking(future -> {
            future.complete(new ApplicationHandler(resourceConfig));
        }, false,
            res -> {
                if (res.succeeded()) {
                    appHandler = (ApplicationHandler) res.result();
                    router.route(baseUri.getPath() + "*")
                        .handler(this)
                        .failureHandler(context -> {
                            LOG.error("Failured called", context.failure());

                        });
                } else {
                    LOG.error("Problem during creation of ApplicationHandler", res.cause());
                }
            });
    }

    @Override
    public void handle(final RoutingContext routingContext) {

        final HttpServerRequest serverRequest = routingContext.request();
        final URI requestUri = URI.create(serverRequest.absoluteURI());

        vertx.getOrCreateContext().put(RoutingContext.class.getName(), routingContext);
        final ContainerRequest request = new ContainerRequest(baseUri, requestUri, serverRequest.method().name(), new VertxSecurityContext(serverRequest), new MapPropertiesDelegate());

        serverRequest.headers().entries().forEach(entry -> request.getHeaders().add(entry.getKey(), entry.getValue()));
        final VertxWebResponseWriter responseWriter = new VertxWebResponseWriter(serverRequest.response());
        request.setWriter(responseWriter);

        if (!serverRequest.isEnded()) {
            final VertxBlockingInputStream is = new VertxBlockingInputStream();
            serverRequest
                .handler(buffer -> is.populate(buffer))
                .endHandler(aVoid -> is.end());
            request.setEntityStream(is);
        }

        vertx.executeBlocking(future -> {
            appHandler.handle(request);
            future.complete();
        },
            false,
            res -> {
                if (res.failed()) {
                    LOG.error("Problem during creation of ApplicationHandler", res.cause());
                }
                routingContext.response().end();
            });

    }
}
