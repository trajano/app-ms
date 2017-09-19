package net.trajano.ms.engine;

import static io.vertx.core.buffer.Buffer.buffer;

import java.io.IOException;
import java.io.OutputStream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

public class VertxOutputStream extends OutputStream {

    private final WriteStream<Buffer> stream;

    public VertxOutputStream(final WriteStream<Buffer> stream) {

        this.stream = stream;
    }

    @Override
    public void close() throws IOException {

        stream.end();
    }

    @Override
    public void write(final byte[] b) throws IOException {

        stream.write(buffer(b));
    }

    @Override
    public void write(final byte[] b,
        final int off,
        final int len) throws IOException {

        stream.write(buffer().appendBytes(b, off, len));
    }

    @Override
    public void write(final int b) throws IOException {

        final Buffer buffer = buffer(new byte[] {
            (byte) b
        });
        stream.write(buffer);

    }
}
