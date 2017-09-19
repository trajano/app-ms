package net.trajano.ms.engine;

import java.net.URI;
import java.util.Collections;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.message.internal.TracingLogger;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.TracingConfig;
import org.glassfish.jersey.server.TracingUtils;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

public class VertxContainer implements
    Handler<HttpServerRequest> {

    private volatile ApplicationHandler appHandler;

    public VertxContainer(final HttpServer http,
        final Class<? extends Application> applicationClass) {

        http.requestHandler(this).listen(8280);
        //                final ResourceConfig config = ResourceConfig.forApplication(application);
        final ResourceConfig config = ResourceConfig.forApplicationClass(applicationClass);
        config.addProperties(Collections.singletonMap(ServerProperties.TRACING, "ALL"));
        appHandler = new ApplicationHandler(config);
        //        config.setClassLoader(this.getClass().getClassLoader());
        //final ResourceConfig application2 = new ResourceConfig();
        //        appHandler = new ApplicationHandler(A);
        //        appHandler = new ApplicationHandler(applicationClass);
        System.out.println(appHandler.getConfiguration().getSingletons());
        System.out.println(appHandler.getConfiguration().getClasses());
    }

    @Override
    public void handle(final HttpServerRequest event) {

        final URI baseUri = URI.create(event.absoluteURI().substring(0, event.absoluteURI().length() - event.uri().length())).resolve("/");
        final URI requestUri = URI.create(event.absoluteURI());

        final ContainerRequest request = new ContainerRequest(baseUri, requestUri, event.method().name(), new VertxSecurityContext(), new MapPropertiesDelegate());
        TracingUtils.initTracingSupport(TracingConfig.ALL, TracingLogger.Level.VERBOSE, request);
        //request.setEntityStream(new VertxInputStream(event));
        System.out.println(request);

        request.setWriter(new VertxWebResponseWriter(event.response()));
        appHandler.handle(request);
        System.out.println(request.getUriInfo().getMatchedResources());
    }

}
