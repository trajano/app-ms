package net.trajano.ms.vertx.jaxrs;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import net.trajano.ms.Microservice;
import net.trajano.ms.core.ConstraintViolationResponse;

@Configuration
@Component
@Provider
@Produces({
    MediaType.APPLICATION_JSON,
    MediaType.APPLICATION_XML,
    MediaType.TEXT_XML,
    MediaType.TEXT_PLAIN
})
public class ConstraintViolationExceptionMapper extends AbstractJsonExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = LoggerFactory.getLogger(Microservice.class);

    @Override
    public Response toResponse(final ConstraintViolationException exception) {

        if (LOG.isDebugEnabled()) {
            LOG.warn("uri={} message={}", uriInfo.getRequestUri(), exception.getMessage(), exception);
            for (final ConstraintViolation<?> violation : exception.getConstraintViolations()) {
                LOG.debug("message={} obj={} path={}", violation.getMessage(), violation.getRootBean(), violation.getPropertyPath());
            }
        } else {
            LOG.warn("uri={} message={}", uriInfo.getRequestUri(), exception.getMessage());
        }
        final MediaType mediaType = getAppropriateMediaType();

        if (mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE) || mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
            return Response.status(Status.BAD_REQUEST)
                .entity(exception.getMessage())
                .type(mediaType)
                .build();
        } else {
            return Response.status(Status.BAD_REQUEST)
                .entity(new ConstraintViolationResponse(exception, uriInfo, showStackTrace))
                .type(mediaType)
                .build();
        }
    }

}
