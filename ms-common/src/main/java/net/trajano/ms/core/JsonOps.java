package net.trajano.ms.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;

public interface JsonOps {

    default <T> T fromJson(final File src,
        final Class<T> to) {

        try (final FileReader reader = new FileReader(src)) {
            return fromJson(reader, to);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default <T> T fromJson(final InputStream src,
        final Class<T> to) {

        return fromJson(new InputStreamReader(src), to);
    }

    <T> T fromJson(Reader src,
        Class<T> to);

    default <T> T fromJson(final String src,
        final Class<T> to) {

        return fromJson(new StringReader(src), to);
    }

    JsonElement toJsonElement(Reader reader);

    default JsonElement toJsonElement(final String s) {

        return toJsonElement(new StringReader(s));
    }

    default void writeTo(final Object src,
        final OutputStream os) throws IOException {

        writeTo(src, new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    void writeTo(Object src,
        Writer os) throws IOException;
}
