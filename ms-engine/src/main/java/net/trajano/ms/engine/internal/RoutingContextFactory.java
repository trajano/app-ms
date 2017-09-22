package net.trajano.ms.engine.internal;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;

import io.vertx.ext.web.RoutingContext;

@Provider
public class RoutingContextFactory implements
    Supplier<RoutingContext> {

    @Inject
    private ContainerRequest request;

    @Override
    public RoutingContext get() {

        return (RoutingContext) request.getProperty(RoutingContext.class.getName());
    }
}
