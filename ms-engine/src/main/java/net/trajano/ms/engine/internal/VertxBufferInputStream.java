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

    /**
     * Reads the next character from the Vert.x buffer. {@inheritDoc}
     */
    @Override
    public int read() throws IOException {

        if (pos == buffer.length()) {
            return -1;
        }

        // Convert to unsigned byte
        return buffer.getByte(pos++) & 0xFF;
    }

    @Override
    public int read(final byte[] b,
        final int off,
        final int len) throws IOException {

        final int size = Math.min(b.length, buffer.length() - pos);
        if (size == 0) {
            return -1;
        }
        buffer.getBytes(pos, pos + size, b, off);
        pos += size;
        return size;
    }

}
