package net.trajano.ms.engine.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

public class VertxBlockingInputStream extends InputStream {

    private static final Buffer END_BUFFER = Symbol.newSymbol(Buffer.class);

    private static final Buffer END_BUFFER_WITH_ERROR = Symbol.newSymbol(Buffer.class);

    private static final Logger LOG = LoggerFactory.getLogger(VertxBlockingInputStream.class);

    private int availableBytes = 0;

    private long bytesRead = 0;

    /**
     * Flag to indicate that the stream is closed.
     */
    private boolean closed = false;

    private Buffer currentBuffer;

    private IOException exceptionToThrow = null;

    private int pos;

    private final BlockingQueue<Buffer> queue = new LinkedBlockingQueue<>();

    /**
     * Constructs VertxBlockingInputStream without any associated handlers
     * configured on a ReadStream.
     */
    public VertxBlockingInputStream() {

    }

    public VertxBlockingInputStream(final ReadStream<Buffer> readStream) {

        readStream
            .handler(buffer -> populate(buffer))
            .endHandler(aVoid -> end());

    }

    @Override
    public int available() throws IOException {

        return availableBytes;
    }

    @Override
    public void close() throws IOException {

        closed = true;
    }

    public void end() {

        queue.add(END_BUFFER);
    }

    /**
     * End the buffer because of an error.
     *
     * @param e
     */
    public void error(final Throwable e) {

        exceptionToThrow = new IOException(e);
        queue.add(END_BUFFER_WITH_ERROR);

    }

    public void populate(final Buffer buffer) {

        queue.add(buffer);
        availableBytes += buffer.length();
    }

    @Override
    public int read() throws IOException {

        if (closed) {
            throw new IOException("Stream is closed");
        }
        if (currentBuffer == null) {
            try {
                currentBuffer = queue.take();
                pos = 0;
            } catch (final InterruptedException e) {
                LOG.error("Interrupted while waiting for next buffer", e);
                Thread.currentThread().interrupt();
            }
        }
        if (currentBuffer == null) {
            throw new IOException("Obtained a null buffer from the queue");
        } else if (currentBuffer == END_BUFFER_WITH_ERROR) {
            throw exceptionToThrow;
        } else if (currentBuffer == END_BUFFER) {
            return -1;
        } else {
            final byte b = currentBuffer.getByte(pos++);
            --availableBytes;
            ++bytesRead;
            if (pos == currentBuffer.length()) {
                currentBuffer = null;
            }
            return b;
        }
    }

    /**
     * Gets a count of how much bytes have been read from this input stream.
     *
     * @return total bytes read
     */
    public long totalBytesRead() {

        return bytesRead;
    }

}
