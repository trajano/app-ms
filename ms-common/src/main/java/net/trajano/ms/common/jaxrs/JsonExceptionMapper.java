package net.trajano.ms.common.jaxrs;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@Provider
public class JsonExceptionMapper implements
    ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonExceptionMapper.class);

    @Context
    private HttpHeaders headers;

    @Value("${error.stackTrace:true}")
    private boolean showStackTrace;

    @Override
    public Response toResponse(final Throwable exception) {

        LOG.error(exception.getMessage(), exception);
        return Response.ok(new ErrorResponse(exception, headers, showStackTrace)).status(Status.INTERNAL_SERVER_ERROR).build();
    }

}
