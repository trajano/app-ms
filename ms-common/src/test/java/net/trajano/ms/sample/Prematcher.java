package net.trajano.ms.sample;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.RoutingContext;

@Provider
public class Prematcher implements
    ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(Prematcher.class);

    @Context
    private RoutingContext routingContext;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        LOG.debug("requestContext={} routingContext={}", requestContext, routingContext);

    }
}
