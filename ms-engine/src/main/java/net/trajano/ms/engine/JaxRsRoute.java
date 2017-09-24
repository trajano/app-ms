package net.trajano.ms.engine;

import static java.util.Collections.singletonMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

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
import net.trajano.ms.engine.internal.VertxBinder;
import net.trajano.ms.engine.internal.VertxBlockingInputStream;
import net.trajano.ms.engine.internal.VertxBufferInputStream;
import net.trajano.ms.engine.internal.VertxSecurityContext;
import net.trajano.ms.engine.internal.VertxWebResponseWriter;

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

        new JaxRsRoute(vertx, router, applicationClass);

    }

    private final ApplicationHandler appHandler;

    private final URI baseUri;

    private JaxRsRoute(
        final Vertx vertx,
        final Router router,
        final Class<? extends Application> applicationClass) {

        final ResourceConfig resourceConfig = new ResourceConfig();
        //        //        System.out.println(resourceConfig.getProperties());
        //        //
        //        final Map<String, Object> properties = new HashMap<>();
        //        properties.put("contextConfig", new ClassPathXmlApplicationContext());
        //resourceConfig.setProperties(properties);
        resourceConfig.register(new VertxBinder(vertx));
        resourceConfig.register(JacksonJaxbJsonProvider.class);

        final String resourcePackage = applicationClass.getPackage().getName();
        resourceConfig.addProperties(singletonMap(ServerProperties.PROVIDER_PACKAGES, resourcePackage));
        resourceConfig.addProperties(singletonMap(ServerProperties.TRACING, "ALL"));

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

        //        appHandler = new ApplicationHandler(resourceConfig);
        appHandler = new ApplicationHandler(resourceConfig);
        System.out.println(appHandler.getConfiguration());
        //
        //        try {
        //            appHandler = new ApplicationHandler(applicationClass.newInstance());
        //        } catch (final InstantiationException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        } catch (final IllegalAccessException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        router.route(baseUri.getPath() + "*").handler(this);
    }

    //    private final Type ContextTYPE = new GenericType<Ref<Context>>() {
    //    }.getType();

    @Override
    public void handle(final RoutingContext routingContext) {

        final HttpServerRequest serverRequest = routingContext.request();
        final URI requestUri = URI.create(serverRequest.absoluteURI());

        final ContainerRequest request = new ContainerRequest(baseUri, requestUri, serverRequest.method().name(), new VertxSecurityContext(serverRequest), new MapPropertiesDelegate());
        request.setProperty(RoutingContext.class.getName(), routingContext);
        //        request.setRequestScopedInitializer(im -> im.register(new VertxRequestBinder(context, vertx)));
        //        request.setRequestScopedInitializer(im -> System.out.println(im.get));
        request.setRequestScopedInitializer(im -> System.out.println("IM=>" + im.getInstance(ContainerRequest.class)));
        //        request.setRequestScopedInitializer(im -> {
        //
        //            im.getInstance(BeanManager.class).getBeans(Object.class).forEach(x -> System.out.println(x.getBeanClass()));
        //            //                    im.inject(vertx.getOrCreateContext());
        //            //                    im.inject(context);
        //            //                    im.inject(event);
        //
        //        });

        serverRequest.headers().entries().forEach(entry -> request.getHeaders().add(entry.getKey(), entry.getValue()));
        request.setWriter(new VertxWebResponseWriter(serverRequest.response()));

        final String contentLengthString = serverRequest.getHeader("Content-Length");
        final int contentLength;
        if (contentLengthString != null) {
            contentLength = Integer.parseInt(contentLengthString);
        } else {
            contentLength = 0;
        }

        if (contentLength == 0) {
            serverRequest
                .endHandler(aVoid -> {
                    appHandler.handle(request);
                    request.close();
                });
        } else if (contentLength < 1024) {
            serverRequest
                .bodyHandler(body -> {
                    request.setEntityStream(new VertxBufferInputStream(body));
                    appHandler.handle(request);
                    request.close();
                });
        } else {
            try (final VertxBlockingInputStream is = new VertxBlockingInputStream(serverRequest)) {
                serverRequest
                    .handler(buffer -> is.populate(buffer))
                    .endHandler(aVoid -> is.end());
                routingContext.vertx().executeBlocking(future -> {
                    request.setEntityStream(is);
                    appHandler.handle(request);
                    future.complete();
                }, false, result -> {
                    request.close();
                });
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
