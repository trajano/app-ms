package net.trajano.ms.vertx.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.trajano.ms.core.JsonOps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

@Component
public class GsonJacksonJsonOps implements
    JsonOps {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Gson gson;

    @Override
    public <T> T fromJson(Reader src,
        Class<T> to) {

        try {
            return objectMapper.readerFor(to).readValue(src);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeTo(Object src,
        Writer writer) throws IOException {

        objectMapper.writer().writeValue(writer, src);
    }

    @Override
    public JsonElement toJsonElement(Reader s) {

        return gson.fromJson(s, JsonElement.class);
    }

}
