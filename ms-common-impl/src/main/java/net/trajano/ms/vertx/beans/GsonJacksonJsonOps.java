package net.trajano.ms.vertx.beans;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.trajano.ms.core.JsonOps;

@Component
public class GsonJacksonJsonOps implements
    JsonOps {

    @Autowired
    private Gson gson;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public <T> T fromJson(final Reader src,
        final Class<T> to) {

        try {
            return objectMapper.readerFor(to).readValue(src);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public JsonElement toJsonElement(final Reader s) {

        return gson.fromJson(s, JsonElement.class);
    }

    @Override
    public void writeTo(final Object src,
        final Writer writer) throws IOException {

        objectMapper.writer().writeValue(writer, src);
    }

}
