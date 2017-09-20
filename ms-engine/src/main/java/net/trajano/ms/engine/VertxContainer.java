package net.trajano.ms.engine;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

public class VertxContainer implements
    Handler<HttpServerRequest> {

    private volatile ApplicationHandler appHandler;

    public VertxContainer(final HttpServer http,
        final Class<? extends Application> applicationClass) {

        http.requestHandler(this).listen(8280);
        //        final ResourceConfig config = ResourceConfig.forApplicationClass(applicationClass);
        //        config.register(Hk2)
        //        final ResourceConfig config = ResourceConfig.forApplicationClass(applicationClass);
        //        config.addProperties(Collections.singletonMap(ServerProperties.TRACING, "ALL"));
        //        appHandler = new ApplicationHandler(config);
        //        //        config.setClassLoader(this.getClass().getClassLoader());
        //        //final ResourceConfig application2 = new ResourceConfig();
        //        //        appHandler = new ApplicationHandler(A);
        appHandler = new ApplicationHandler(applicationClass);
    }

    @Override
    public void handle(final HttpServerRequest event) {

        final URI baseUri = URI.create(event.absoluteURI().substring(0, event.absoluteURI().length() - event.uri().length())).resolve("/");
        final URI requestUri = URI.create(event.absoluteURI());

        final ContainerRequest request = new ContainerRequest(baseUri, requestUri, event.method().name(), new VertxSecurityContext(), new MapPropertiesDelegate());

        event.headers().entries().forEach(entry -> {
            request.getHeaders().add(entry.getKey(), entry.getValue());
        });
        request.setWriter(new VertxWebResponseWriter(event.response()));

        //        event
        //            .bodyHandler(buffer -> {
        //                request.setEntityStream(new VertxBufferInputStream(buffer));
        //                appHandler.handle(request);
        //            });
        //
        final VertxInputStream input = new VertxInputStream(event);
        request.setEntityStream(input);

        final AtomicBoolean handling = new AtomicBoolean(false);

        event
            .endHandler(aVoid -> {
                System.out.println("END");
                input.end();
                if (!handling.getAndSet(true)) {
                    appHandler.handle(request);
                }
            })
            .handler(buffer -> {
                System.out.println("BUF");
                input.populate(buffer);
                if (!handling.getAndSet(true)) {
                    appHandler.handle(request);
                }
            });

    }

}
