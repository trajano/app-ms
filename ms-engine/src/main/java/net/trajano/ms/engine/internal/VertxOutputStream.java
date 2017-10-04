package net.trajano.ms.engine.internal;

import static io.vertx.core.buffer.Buffer.buffer;

import java.io.IOException;
import java.io.OutputStream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.WriteStream;

public class VertxOutputStream extends OutputStream {

    private final WriteStream<Buffer> stream;

    public VertxOutputStream(final WriteStream<Buffer> stream) {

        this.stream = stream;
    }

    private void addChunkedIfNeeded() {

        if (stream instanceof HttpServerResponse) {
            final HttpServerResponse resp = (HttpServerResponse) stream;
            if (resp.headers().get(HttpHeaders.CONTENT_LENGTH) == null) {
                resp.setChunked(true);
            }
        } else if (stream instanceof HttpClientRequest) {
            final HttpClientRequest req = (HttpClientRequest) stream;
            if (req.headers().get(HttpHeaders.CONTENT_LENGTH) == null) {
                req.setChunked(true);
            }
        }

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void write(final byte[] b) throws IOException {

        addChunkedIfNeeded();
        stream.write(buffer(b));
    }

    @Override
    public void write(final byte[] b,
        final int off,
        final int len) throws IOException {

        addChunkedIfNeeded();
        stream.write(buffer().appendBytes(b, off, len));
    }

    @Override
    public void write(final int b) throws IOException {

        addChunkedIfNeeded();
        final Buffer buffer = buffer(new byte[] {
            (byte) b
        });
        stream.write(buffer);

    }
}
