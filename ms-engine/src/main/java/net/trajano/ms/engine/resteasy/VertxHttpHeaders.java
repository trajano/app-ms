package net.trajano.ms.engine.resteasy;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import io.vertx.core.http.HttpServerRequest;

public class VertxHttpHeaders implements
    HttpHeaders {

    private final HttpServerRequest vertxRequest;

    public VertxHttpHeaders(final HttpServerRequest vertxRequest) {

        this.vertxRequest = vertxRequest;
    }

    @Override
    public List<Locale> getAcceptableLanguages() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Cookie> getCookies() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getDate() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHeaderString(final String paramString) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Locale getLanguage() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLength() {

        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public MediaType getMediaType() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getRequestHeader(final String paramString) {

        vertxRequest.getHeader(paramString);
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {

        // TODO Auto-generated method stub
        return null;
    }

}
