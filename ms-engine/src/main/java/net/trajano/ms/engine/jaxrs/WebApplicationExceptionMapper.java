package net.trajano.ms.engine.jaxrs;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

/**
 * Provides the customized object mapper to the JAX-RS environment.
 *
 * @author Archimedes Trajano
 */
@Component
@Provider
@Priority(Priorities.ENTITY_CODER)
public class WebApplicationExceptionMapper implements
    ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(final WebApplicationException exception) {

        return exception.getResponse();
    }
}
