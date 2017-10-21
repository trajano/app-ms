package net.trajano.ms.vertx.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

@Component
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class GsonMessageBodyHandler implements
    MessageBodyWriter<JsonElement>,
    MessageBodyReader<JsonElement> {

    private final Gson gson;

    public GsonMessageBodyHandler() {

        gson = new GsonBuilder().create();
    }

    @Override
    public long getSize(final JsonElement t,
        final Class<?> type,
        final Type genericType,
        final Annotation[] annotations,
        final MediaType mediaType) {

        return -1;
    }

    @Override
    public boolean isReadable(final Class<?> type,
        final Type genericType,
        final Annotation[] annotations,
        final MediaType mediaType) {

        return true;
    }

    @Override
    public boolean isWriteable(final Class<?> type,
        final Type genericType,
        final Annotation[] annotations,
        final MediaType mediaType) {

        return true;
    }

    @Override
    public JsonElement readFrom(final Class<JsonElement> type,
        final Type genericType,
        final Annotation[] annotations,
        final MediaType mediaType,
        final MultivaluedMap<String, String> httpHeaders,
        final InputStream entityStream) throws IOException {

        try (final Reader r = new InputStreamReader(entityStream, StandardCharsets.UTF_8)) {
            return gson.fromJson(r, JsonElement.class);
        }
    }

    @Override
    public void writeTo(final JsonElement t,
        final Class<?> type,
        final Type genericType,
        final Annotation[] annotations,
        final MediaType mediaType,
        final MultivaluedMap<String, Object> httpHeaders,
        final OutputStream entityStream) throws IOException {

        try (final OutputStreamWriter w = new OutputStreamWriter(entityStream)) {
            gson.toJson(t, new JsonWriter(w));
        }

    }
}
