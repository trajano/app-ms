package net.trajano.ms.engine.internal;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper implements
    ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(final WebApplicationException exception) {

        if (exception.getResponse() != null) {
            return exception.getResponse();
        }
        if (exception instanceof NotFoundException) {
            return Response.ok(exception.getLocalizedMessage()).status(Status.NOT_FOUND);
        }
    }

}
