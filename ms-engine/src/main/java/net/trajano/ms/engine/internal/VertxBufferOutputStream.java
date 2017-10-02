package net.trajano.ms.engine.internal;

import java.io.IOException;
import java.io.OutputStream;

import io.vertx.core.buffer.Buffer;

public class VertxBufferOutputStream extends OutputStream {

    private final Buffer buffer;

    public VertxBufferOutputStream() {

        this(Buffer.buffer());
    }

    public VertxBufferOutputStream(final Buffer buffer) {

        this.buffer = buffer;
    }

    public Buffer getBuffer() {

        return buffer;
    }

    @Override
    public void write(final byte[] b) throws IOException {

        buffer.appendBytes(b);
    }

    @Override
    public void write(final byte[] b,
        final int off,
        final int len) throws IOException {

        buffer.appendBytes(b, off, len);
    }

    @Override
    public void write(final int b) throws IOException {

        buffer.appendByte((byte) b);

    }

}
