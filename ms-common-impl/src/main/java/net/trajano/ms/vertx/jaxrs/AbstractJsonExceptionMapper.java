package net.trajano.ms.vertx.jaxrs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import net.trajano.ms.Microservice;

public abstract class AbstractJsonExceptionMapper<T extends Throwable> implements
    ExceptionMapper<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Microservice.class);

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

    @Value("${microservice.show_stack_trace:#{null}}")
    protected Boolean showStackTrace;

    @Context
    protected UriInfo uriInfo;

    /**
     * Determines the appropriate media type based on what is requested. If wildcard
     * use JSON.
     *
     * @return media type appropriate for request
     */
    protected MediaType getAppropriateMediaType() {

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
     * This sets the context data so the mapper can be unit tested.
     */
    public void setContextData(final HttpHeaders headers,
        final UriInfo uriInfo,
        final boolean showStackTrace) {

        this.headers = headers;
        this.uriInfo = uriInfo;
        this.showStackTrace = showStackTrace;

    }

    /**
     * If the show request URI or show stack trace are not defined, it will default
     * to whether the current logger is on debug mode or not.
     */
    @PostConstruct
    public void setDebugFlags() {

        if (showStackTrace == null) {
            showStackTrace = LOG.isDebugEnabled();
            LOG.debug("stack trace enabled if this is shown");
        }
    }

}
