package net.trajano.ms.engine.sample;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import io.vertx.ext.web.RoutingContext;

@Provider
public class Prematcher implements
    ContainerRequestFilter {

    @Inject
    private RoutingContext routingContext;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        System.out.println("" + requestContext + " " + routingContext);

    }
}
