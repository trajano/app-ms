package net.trajano.ms.engine;

import java.net.URI;
import java.security.Principal;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

public class VertxContainer implements
    Container,
    Handler<HttpServerRequest> {

    private volatile ApplicationHandler appHandler;

    public VertxContainer(final HttpServer http,
        final Application application) {

        System.out.println(http);
        http.requestHandler(this).listen(8280);
        appHandler = new ApplicationHandler(application);
    }

    @Override
    public ApplicationHandler getApplicationHandler() {

        return appHandler;
    }

    @Override
    public ResourceConfig getConfiguration() {

        return appHandler.getConfiguration();
    }

    @Override
    public void handle(final HttpServerRequest event) {

        final URI baseUri = URI.create(event.absoluteURI().substring(0, event.absoluteURI().length() - event.uri().length()));
        final URI requestUri = URI.create(event.absoluteURI());

        final ContainerRequest request = new ContainerRequest(baseUri, requestUri, event.method().name(), new SecurityContext() {

            @Override
            public String getAuthenticationScheme() {

                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Principal getUserPrincipal() {

                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isSecure() {

                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isUserInRole(final String paramString) {

                // TODO Auto-generated method stub
                return false;
            }
        }, new MapPropertiesDelegate());

        request.setWriter(new VertxWebResponseWriter(event.response()));
        appHandler.handle(request);
        //        final Future<ContainerResponse> future = appHandler.apply(request);
        //        final ContainerResponse resp = future.get();
    }

    @Override
    public void reload() {

        reload(getConfiguration());
    }

    @Override
    public void reload(final ResourceConfig configuration) {

        appHandler.onShutdown(this);

        appHandler = new ApplicationHandler(configuration);
        appHandler.onReload(this);
        appHandler.onStartup(this);

    }

}
