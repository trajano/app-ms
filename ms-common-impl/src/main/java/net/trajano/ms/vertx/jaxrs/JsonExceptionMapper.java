package net.trajano.ms.vertx.jaxrs;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import net.trajano.ms.Microservice;
import net.trajano.ms.core.ErrorResponse;

@Configuration
@Component
@Provider
@Produces({
    MediaType.APPLICATION_JSON,
    MediaType.APPLICATION_XML,
    MediaType.TEXT_XML,
    MediaType.TEXT_PLAIN
})
public class JsonExceptionMapper extends AbstractJsonExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(Microservice.class);

    /**
     * Log the exception if it is not NotFoundException and only use warn if it is a
     * client error.
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
        int status = Status.INTERNAL_SERVER_ERROR.getStatusCode();
        if (exception instanceof WebApplicationException) {
            final WebApplicationException internalException = (WebApplicationException) exception;
            if (internalException.getResponse().hasEntity()) {
                return internalException.getResponse();
            } else {
                status = internalException.getResponse().getStatus();
            }
        }
        final MediaType mediaType = getAppropriateMediaType();

        if (mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE) || mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
            return Response.status(status)
                .entity(exception.getMessage())
                .type(mediaType)
                .build();
        } else {
            return Response.status(status)
                .entity(new ErrorResponse(exception, uriInfo, showStackTrace))
                .type(mediaType)
                .build();
        }
    }

}
