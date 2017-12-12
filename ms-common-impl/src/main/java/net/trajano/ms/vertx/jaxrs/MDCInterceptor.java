package net.trajano.ms.vertx.jaxrs;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.trajano.ms.spi.MDCKeys;
import net.trajano.ms.spi.MicroserviceEngine;

/**
 * This populates the MDC based on the data available on the request. This will
 * skip the method and request URI if debug is not enabled.
 *
 * @author Archimedes Trajano
 */
@Component
@Provider
@Priority(Priorities.AUTHORIZATION)
public class MDCInterceptor implements
    ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(MDCInterceptor.class);

    @Autowired
    private MicroserviceEngine engine;

    @Override
    public void filter(final ContainerRequestContext requestContext) {

        MDC.put(MDCKeys.REQUEST_ID, requestContext.getHeaderString(MDCKeys.REQUEST_ID));
        if (LOG.isDebugEnabled()) {
            MDC.put(MDCKeys.REQUEST_METHOD, requestContext.getMethod());
            MDC.put(MDCKeys.REQUEST_URI, requestContext.getUriInfo().getRequestUri().toASCIIString());
            MDC.put(MDCKeys.HOST, engine.hostname() + ":" + engine.port());
        }

    }

}
