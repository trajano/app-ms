package net.trajano.ms.engine.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import io.vertx.core.json.JsonObject;
import net.trajano.ms.engine.internal.Conversions;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VertxJsonProvider implements
    MessageBodyReader<JsonObject>,
    MessageBodyWriter<JsonObject> {

    @Override
    public long getSize(final JsonObject t,
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

        return type.isAssignableFrom(JsonObject.class);
    }

    @Override
    public boolean isWriteable(final Class<?> type,
        final Type genericType,
        final Annotation[] annotations,
        final MediaType mediaType) {

        return type.isAssignableFrom(JsonObject.class);
    }

    @Override
    public JsonObject readFrom(final Class<JsonObject> type,
        final Type genericType,
        final Annotation[] annotations,
        final MediaType mediaType,
        final MultivaluedMap<String, String> httpHeaders,
        final InputStream entityStream) throws IOException,
        WebApplicationException {

        return new JsonObject(Conversions.toBuffer(entityStream));
    }

    @Override
    public void writeTo(final JsonObject t,
        final Class<?> type,
        final Type genericType,
        final Annotation[] annotations,
        final MediaType mediaType,
        final MultivaluedMap<String, Object> httpHeaders,
        final OutputStream entityStream) throws IOException,
        WebApplicationException {

        entityStream.write(t.encode().getBytes(StandardCharsets.UTF_8));

    }

}
