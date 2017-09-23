package net.trajano.ms.engine;

import java.net.URI;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.resteasy.VertxHttpRequest;

public class JaxRsRoute implements
    Handler<RoutingContext> {

    /**
     * Constructs a new route for the given router to a JAX-RS application.
     *
     * @param router
     * @param applicationClass
     */
    public static void route(final Vertx vertx,
        final Router router,
        final Class<? extends Application> applicationClass) {

        new JaxRsRoute(router, applicationClass);

    }

    private final URI baseUri;

    final ResteasyProviderFactory providerFactory;

    private JaxRsRoute(
        final Router router,
        final Class<? extends Application> applicationClass) {

        providerFactory = ResteasyProviderFactory.getInstance();
        providerFactory.register(JacksonJaxbJsonProvider.class);

        final String resourcePackage = applicationClass.getPackage().getName();

        final ApplicationPath annotation = applicationClass.getAnnotation(ApplicationPath.class);
        if (annotation != null) {
            baseUri = URI.create(annotation.value() + "/").normalize();
        } else {
            baseUri = URI.create("/");
        }

        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage(resourcePackage);
        beanConfig.setScan(true);
        beanConfig.setBasePath(baseUri.getPath());
        beanConfig.scanAndRead();

        try {
            final Swagger swagger = beanConfig.getSwagger();
            final String json = Json.mapper().writeValueAsString(swagger);
            final String yaml = Yaml.mapper().writeValueAsString(swagger);
            router.get(baseUri.getPath()).produces("application/json").handler(context -> context.response().putHeader("Content-Type", "application/json").end(json));
            router.get(baseUri.getPath()).produces("application/yaml").handler(context -> context.response().putHeader("Content-Type", "application/yaml").end(yaml));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        router.route(baseUri.getPath() + "*").handler(this);
    }

    @Override
    public void handle(final RoutingContext routingContext) {

        final HttpServerRequest serverRequest = routingContext.request();

        final SynchronousDispatcher dispatcher = new SynchronousDispatcher(providerFactory);
        final HttpRequest request = new VertxHttpRequest(baseUri, serverRequest);
        final HttpResponse response = new VertxHttpResponse(serverRequest.response());
        System.out.println("HERE" + dispatcher.getRegistry().getSize());
        routingContext.vertx().executeBlocking(f -> {
            dispatcher.invoke(request, response);
            f.succeeded();
        }, res -> {
            System.out.println("res");
        });
    }

}
