package net.trajano.ms.vertx.jaxrs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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
public class JsonExceptionMapper implements
    ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonExceptionMapper.class);

    /**
     * Supported media types.
     */
    private static final Set<MediaType> SUPPORTED_MEDIA_TYPES = new HashSet<>(Arrays.asList(
        MediaType.APPLICATION_JSON_TYPE,
        MediaType.APPLICATION_XML_TYPE,
        MediaType.TEXT_XML_TYPE,
        MediaType.TEXT_PLAIN_TYPE,
        MediaType.TEXT_HTML_TYPE));

    @Context
    private HttpHeaders headers;

    @Value("${microservice.show_request_uri:#{null}}")
    private Boolean showRequestUri;

    @Value("${microservice.show_stack_trace:#{null}}")
    private Boolean showStackTrace;

    @Context
    private UriInfo uriInfo;

    /**
     * Determines the appropriate media type based on what is requested. If
     * wildcard use JSON.
     *
     * @return media type appropriate for request
     */
    private MediaType getAppropriateMediaType() {

        final List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
        for (final MediaType mediaType : acceptableMediaTypes) {
            if (mediaType.equals(MediaType.WILDCARD_TYPE)) {
                return MediaType.APPLICATION_JSON_TYPE;
            } else if (SUPPORTED_MEDIA_TYPES.contains(mediaType)) {
                return mediaType;
            }
        }
        return MediaType.APPLICATION_JSON_TYPE;

    }

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

    /**
     * This sets the context data so the mapper can be unit tested.
     */
    public void setContextData(final HttpHeaders headers,
        final UriInfo uriInfo,
        final boolean showRequestUri,
        final boolean showStackTrace) {

        this.headers = headers;
        this.uriInfo = uriInfo;
        this.showRequestUri = showRequestUri;
        this.showStackTrace = showStackTrace;

    }

    /**
     * If the show request URI or show stack trace are not defined, it will
     * default to whether the current logger is on debug mode or not.
     */
    @PostConstruct
    public void setDebugFlags() {

        if (showRequestUri == null) {
            showRequestUri = LOG.isDebugEnabled();
        }
        if (showStackTrace == null) {
            showStackTrace = LOG.isDebugEnabled();
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
                .entity(new ErrorResponse(exception, headers, uriInfo, showStackTrace, showRequestUri))
                .type(mediaType)
                .build();
        }
    }

}
