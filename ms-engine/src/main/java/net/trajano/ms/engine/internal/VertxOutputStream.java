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

    private Thread closingThread;

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

    /**
     * Checks if the stream has been closed by the current thread. If so it should
     * not allow usage anymore.
     *
     * @throws IOException
     *             when the stream is closed.
     */
    private void checkClosed() throws IOException {

        if (closingThread == Thread.currentThread()) {
            throw new IOException("Cannot use on the same thread that had closed it");
        }
    }

    @Override
    public void close() throws IOException {

        if (closingThread != null && closingThread != Thread.currentThread()) {
            throw new IOException("Attempt to close from another thread");
        }
        closingThread = Thread.currentThread();
    }

    @Override
    public void write(final byte[] b) throws IOException {

        checkClosed();
        addChunkedIfNeeded();
        stream.write(buffer(b));
    }

    @Override
    public void write(final byte[] b,
        final int off,
        final int len) throws IOException {

        checkClosed();
        addChunkedIfNeeded();
        stream.write(buffer().appendBytes(b, off, len));
    }

    @Override
    public void write(final int b) throws IOException {

        checkClosed();
        addChunkedIfNeeded();
        final Buffer buffer = buffer(new byte[] {
            (byte) b
        });
        stream.write(buffer);

    }
}
