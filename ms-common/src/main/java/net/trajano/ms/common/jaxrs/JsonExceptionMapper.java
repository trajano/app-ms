package net.trajano.ms.common.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

@Component
@Provider
public class JsonExceptionMapper implements
    ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(final Throwable exception) {

        return Response.ok(new ErrorResponse(exception)).status(Status.INTERNAL_SERVER_ERROR).build();
    }

}
