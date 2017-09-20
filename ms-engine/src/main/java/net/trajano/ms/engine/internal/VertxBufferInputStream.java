package net.trajano.ms.engine.internal;

import java.io.IOException;
import java.io.InputStream;

import io.vertx.core.buffer.Buffer;

public class VertxBufferInputStream extends InputStream {

    private final Buffer buffer;

    private int pos;

    public VertxBufferInputStream(final Buffer buffer) {

        this.buffer = buffer;
        pos = 0;

    }

    @Override
    public int read() throws IOException {

        if (pos == buffer.length()) {
            return -1;
        }
        return buffer.getByte(pos++);
    }

}
