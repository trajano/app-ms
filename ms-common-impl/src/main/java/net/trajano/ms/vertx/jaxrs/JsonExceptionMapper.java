package net.trajano.ms.vertx.jaxrs;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import net.trajano.ms.vertx.beans.ErrorResponse;

@Configuration
@Component
@Provider
public class JsonExceptionMapper implements
    ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonExceptionMapper.class);

    @Context
    private HttpHeaders headers;

    @Value("${microservice.show_request_uri:true}")
    private Boolean showRequestUri;

    @Value("${microservice.show_stack_trace:true}")
    private Boolean showStackTrace;

    @Context
    private UriInfo uriInfo;

    /**
     * Log the exception if it is not NotFoundException and only use warn if it
     * is a client error.
     *
     * @param exception
     */
    private void log(final Throwable exception) {

        if (exception instanceof ClientErrorException) {
            if (!(exception instanceof NotFoundException)) {
                LOG.warn("uri={} message={}", uriInfo.getRequestUri(), exception.getMessage(), exception);
            }
        } else {
            LOG.error("uri={} message={}", uriInfo.getRequestUri(), exception.getMessage(), exception);
        }
    }

    @Override
    public Response toResponse(final Throwable exception) {

        log(exception);
        if (exception instanceof WebApplicationException) {
            final WebApplicationException internalException = (WebApplicationException) exception;
            if (internalException.getResponse().hasEntity()) {
                return internalException.getResponse();
            }
        }
        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
        if (headers.getAcceptableMediaTypes().contains(MediaType.APPLICATION_XML_TYPE) && !headers.getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            mediaType = MediaType.APPLICATION_XML_TYPE;
        }
        return Response.serverError()
            .entity(new ErrorResponse(exception, headers, uriInfo, showStackTrace, showRequestUri))
            .type(mediaType)
            .build();
    }

}
