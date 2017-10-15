package net.trajano.ms.common.jaxrs;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import io.vertx.ext.web.RoutingContext;

/**
 * This adds the request ID from the header to the MDC if it is available.
 *
 * @author Archimedes Trajano
 */
@Component
@Provider
@Priority(Priorities.AUTHORIZATION + 1)
@PreMatching
public class RequestIdContextInterceptor implements
    ContainerRequestFilter {

    /**
     * Request ID Header and MDC key.
     */
    public static final String REQUEST_ID = "X-Request-ID";

    @Context
    private RoutingContext routingContext;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        final String requestID = requestContext.getHeaderString(REQUEST_ID);
        if (requestID != null) {
            MDC.put(REQUEST_ID, requestID);
        }
    }

}
