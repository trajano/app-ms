package net.trajano.ms.common.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Provider
public class JsonExceptionMapper implements
    ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonExceptionMapper.class);

    @Override
    public Response toResponse(final Throwable exception) {

        LOG.error(exception.getMessage(), exception);
        return Response.ok(new ErrorResponse(exception)).status(Status.INTERNAL_SERVER_ERROR).build();
    }

}
