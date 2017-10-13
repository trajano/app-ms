package net.trajano.ms.engine.internal.spring;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.springframework.web.context.request.AbstractRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Provider
@PreMatching
public final class VertxRequestContextFilter implements
    ContainerRequestFilter,
    ContainerResponseFilter {

    private static final String REQUEST_ATTRIBUTES_PROPERTY = VertxRequestContextFilter.class.getName() + ".REQUEST_ATTRIBUTES";

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        final RequestAttributes attributes = new VertxHttpRequestAttributes(requestContext);
        requestContext.setProperty(REQUEST_ATTRIBUTES_PROPERTY, attributes);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext,
        final ContainerResponseContext responseContext)
        throws IOException {

        final AbstractRequestAttributes attributes = (AbstractRequestAttributes) requestContext.getProperty(REQUEST_ATTRIBUTES_PROPERTY);
        RequestContextHolder.resetRequestAttributes();
        attributes.requestCompleted();
    }
}
