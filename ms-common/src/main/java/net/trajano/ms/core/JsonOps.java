package net.trajano.ms.core;

import com.google.gson.JsonElement;

import java.io.*;
import java.nio.charset.StandardCharsets;

public interface JsonOps {

    default <T> T fromJson(String src,
        Class<T> to) {

        return fromJson(new StringReader(src), to);
    }

    <T> T fromJson(Reader src,
        Class<T> to);

    default <T> T fromJson(InputStream src,
        Class<T> to) {

        return fromJson(new InputStreamReader(src), to);
    }

    default void writeTo(Object src,
        OutputStream os) throws IOException {

        writeTo(src, new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    void writeTo(Object src,
        Writer os) throws IOException;

    default JsonElement toJsonElement(String s) {

        return toJsonElement(new StringReader(s));
    }

    JsonElement toJsonElement(Reader reader);
}
