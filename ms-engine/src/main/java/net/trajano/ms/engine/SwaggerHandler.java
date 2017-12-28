package net.trajano.ms.engine;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.reflections.Reflections;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.swagger.ClonableSwagger;

public class SwaggerHandler implements
    Handler<RoutingContext>,
    AutoCloseable {

    /**
     * Convenience method to construct and register the routes to a Vert.x router
     * with a base Spring application context.
     *
     * @param router
     *            vert.x router
     * @param applicationClasses
     *            application classes
     * @return the handlers
     */
    @SafeVarargs
    public static SwaggerHandler[] multipleRegisterToRouter(final Router router,
        final Class<? extends Application>... applicationClasses) {

        final SwaggerHandler[] ret = new SwaggerHandler[applicationClasses.length];
        int i = 0;
        for (final Class<? extends Application> applicationClass : applicationClasses) {
            final SwaggerHandler requestHandler = new SwaggerHandler(applicationClass);
            router.get(requestHandler.baseUriRoute())
                .useNormalisedPath(true)
                .handler(requestHandler);
            ret[i++] = requestHandler;
        }
        return ret;

    }

    /**
     * Convenience method to construct and register a single application route to a
     * Vert.x router.
     *
     * @param router
     *            vert.x router
     * @param applicationClass
     *            application class
     * @return the handler
     */
    public static SwaggerHandler registerToRouter(final Router router,
        final Class<? extends Application> applicationClass) {

        return multipleRegisterToRouter(router, applicationClass)[0];
    }

    private final URI baseUri;

    private final ClonableSwagger swagger;

    public SwaggerHandler(
        final Class<? extends Application> applicationClass) {

        final ApplicationPath annotation = applicationClass.getAnnotation(ApplicationPath.class);
        if (annotation != null) {
            baseUri = URI.create(annotation.value()).normalize();
        } else {
            baseUri = URI.create("/");
        }

        Application application;
        try {
            application = applicationClass.newInstance();
        } catch (InstantiationException
            | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }

        swagger = new ClonableSwagger();
        final Reader swaggerReader = new Reader(swagger);
        final Set<Class<?>> resourceClasses = application.getClasses();
        if (resourceClasses.isEmpty()) {
            final String packageName = applicationClass.getPackage().getName();
            final Reflections reflections = new Reflections(packageName);
            reflections.getTypesAnnotatedWith(Api.class).forEach(swaggerReader::read);
            reflections.getTypesAnnotatedWith(SwaggerDefinition.class).forEach(swaggerReader::read);
        } else {
            swaggerReader.read(applicationClass);
            resourceClasses.forEach(swaggerReader::read);
        }

    }

    /**
     * The base URI set in {@link ApplicationPath} annotation.
     *
     * @return the base URI
     */
    public URI baseUri() {

        return baseUri;
    }

    /**
     * The route path to the base URI. Basically base URI + "/*".
     *
     * @return The route path to the base URI.
     */
    public String baseUriRoute() {

        return baseUri().getPath();
    }

    @Override
    public void close() {

        // does nothing.
    }

    public Swagger getSwagger() {

        return swagger;
    }

    @Override
    public void handle(final RoutingContext context) {

        try {
            context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .end(Buffer.buffer(io.swagger.util.Json.mapper()
                    .writeValueAsBytes(swagger.withRoutingContext(context))));
        } catch (final JsonProcessingException e) {
            context.fail(e);
        }
    }

}
