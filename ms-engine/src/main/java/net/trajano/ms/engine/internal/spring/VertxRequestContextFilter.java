package net.trajano.ms.engine.internal.spring;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

/**
 * This is used to set the request scoped objects.
 *
 * @author Archimedes Trajano
 */
@Provider
@PreMatching
public final class VertxRequestContextFilter implements
    ContainerRequestFilter,
    ContainerResponseFilter {

    @Override
    public void filter(final ContainerRequestContext containerRequest) throws IOException {

        ContainerRequestScope.setRequestContext(containerRequest);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext,
        final ContainerResponseContext responseContext)
        throws IOException {

        ContainerRequestScope.resetRequestContext();
    }
}
