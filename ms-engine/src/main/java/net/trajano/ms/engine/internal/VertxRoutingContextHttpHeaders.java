package net.trajano.ms.engine.internal;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import io.vertx.ext.web.RoutingContext;

/**
 * There's a bug in RestEasy's implementation of HTTP headers in that it is not
 * case insensitive when looking up keys.
 *
 * @author Archimedes Trajano
 */
public class VertxRoutingContextHttpHeaders implements
    HttpHeaders {

    private final RoutingContext context;

    private final MediaType mediaType;

    public VertxRoutingContextHttpHeaders(final RoutingContext context) {

        this.context = context;

        final String obj = getHeaderString(CONTENT_TYPE);
        if (obj == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        } else {
            mediaType = MediaType.valueOf(obj);
        }
    }

    @Override
    public List<Locale> getAcceptableLanguages() {

        return context.acceptableLanguages().stream().map(r -> {
            if (r.subtagCount() == 0) {
                return new Locale(r.tag());
            } else if (r.subtagCount() == 1) {
                return new Locale(r.tag(), r.subtag());
            } else {
                return new Locale(r.tag(), r.subtag(), r.subtag(2));
            }
        }).collect(Collectors.toList());

    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {

        final String acceptableContentType = context.getAcceptableContentType();
        if (acceptableContentType == null) {
            return Collections.singletonList(MediaType.WILDCARD_TYPE);
        }
        return Collections.singletonList(MediaType.valueOf(acceptableContentType));
    }

    @Override
    public Map<String, Cookie> getCookies() {

        return Conversions.toCookies(context.cookies());
    }

    @Override
    public Date getDate() {

        final String date = context.request().getHeader(HttpHeaders.DATE);
        if (date == null) {
            return null;
        }
        return Date.from(Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeaderString(final String name) {

        return context.request().getHeader(name);
    }

    @Override
    public Locale getLanguage() {

        return new Locale(getHeaderString(CONTENT_LANGUAGE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {

        final String obj = getHeaderString(CONTENT_LENGTH);
        if (obj == null) {
            return -1;
        }
        return Integer.parseInt(obj);
    }

    @Override
    public MediaType getMediaType() {

        return mediaType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRequestHeader(final String name) {

        return Collections.singletonList(getHeaderString(name));
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {

        return Conversions.toMultivaluedStringMap(context.request().headers());
    }

}
