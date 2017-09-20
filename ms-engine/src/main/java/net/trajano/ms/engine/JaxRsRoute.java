package net.trajano.ms.engine;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.Response.Status.REQUEST_ENTITY_TOO_LARGE;

import java.net.URI;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.ApiListingResource;
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

    private volatile ApplicationHandler appHandler;

    private final URI baseUri;

    private final Vertx vertx;

    private JaxRsRoute(final Vertx vertx,
        final Router router,
        final Class<? extends Application> applicationClass) {

        this.vertx = vertx;
        final ResourceConfig resourceConfig = ResourceConfig.forApplicationClass(applicationClass);
        resourceConfig.register(JacksonJaxbJsonProvider.class);
        resourceConfig.register(ApiListingResource.class);
        resourceConfig.register(SwaggerSerializers.class);

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
        beanConfig.getSwagger();

        appHandler = new ApplicationHandler(resourceConfig);
        router.route(baseUri.getPath() + "*").handler(this);
    }

    @Override
    public void handle(final RoutingContext context) {

        final HttpServerRequest event = context.request();
        final URI requestUri = URI.create(event.absoluteURI());

        final ContainerRequest request = new ContainerRequest(baseUri, requestUri, event.method().name(), new VertxSecurityContext(event), new MapPropertiesDelegate());

        event.headers().entries().forEach(entry -> {
            request.getHeaders().add(entry.getKey(), entry.getValue());
        });
        request.setWriter(new VertxWebResponseWriter(event.response()));

        final String contentLengthString = event.getHeader("Content-Length");
        final int contentLength;
        if (contentLengthString != null) {
            contentLength = Integer.parseInt(contentLengthString);
        } else {
            contentLength = 0;
        }

        if (contentLength == 0) {
            final Buffer body = Buffer.buffer();
            event
                .handler(buffer -> {
                    if (!event.response().headWritten()) {
                        body.appendBuffer(buffer);
                        if (body.length() > 10 * 1024 * 1024) {
                            event.response()
                                .setStatusCode(REQUEST_ENTITY_TOO_LARGE.getStatusCode())
                                .setStatusMessage(REQUEST_ENTITY_TOO_LARGE.getReasonPhrase())
                                .end();
                        }
                    }
                })
                .endHandler(aVoid -> {
                    request.setEntityStream(new VertxBufferInputStream(body));
                    appHandler.handle(request);
                });
        } else if (contentLength < 1024) {
            event
                .bodyHandler(body -> {
                    request.setEntityStream(new VertxBufferInputStream(body));
                    appHandler.handle(request);
                });
        } else {
            final VertxBlockingInputStream is = new VertxBlockingInputStream(event);
            System.out.println("Here " + is);
            event
                .handler(buffer -> {
                    is.populate(buffer);
                })
                .endHandler(aVoid -> is.end());
            vertx.executeBlocking(future -> {
                request.setEntityStream(is);
                appHandler.handle(request);
            }, false, result -> {

            });
        }
    }

}
