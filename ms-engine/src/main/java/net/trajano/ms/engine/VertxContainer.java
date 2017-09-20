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

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

public class VertxContainer implements
    Handler<HttpServerRequest> {

    private volatile ApplicationHandler appHandler;

    private final URI baseUri;

    public VertxContainer(final HttpServer http,
        final Class<? extends Application> applicationClass) {

        http.requestHandler(this).listen(8280);
        final ResourceConfig resourceConfig = ResourceConfig.forApplicationClass(applicationClass);
        resourceConfig.addProperties(singletonMap(ServerProperties.PROVIDER_PACKAGES, applicationClass.getPackage().getName()));
        final ApplicationPath annotation = applicationClass.getAnnotation(ApplicationPath.class);
        if (annotation != null) {
            baseUri = URI.create(annotation.value() + "/").normalize();
        } else {
            baseUri = URI.create("/");
        }
        appHandler = new ApplicationHandler(resourceConfig);
    }

    @Override
    public void handle(final HttpServerRequest event) {

        final URI requestUri = URI.create(event.absoluteURI());

        final ContainerRequest request = new ContainerRequest(baseUri, requestUri, event.method().name(), new VertxSecurityContext(event), new MapPropertiesDelegate());

        event.headers().entries().forEach(entry -> {
            request.getHeaders().add(entry.getKey(), entry.getValue());
        });
        request.setWriter(new VertxWebResponseWriter(event.response()));

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
    }

}
