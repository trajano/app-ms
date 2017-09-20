package net.trajano.ms.engine;

import java.io.IOException;
import java.io.InputStream;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

class Foo implements
    WriteStream<Buffer> {

    private Buffer data;

    private boolean ended = false;

    private int streamBufferPos;

    private Handler<Void> theDrainHandler;

    private Handler<Throwable> theExceptionHandler;

    private int writeQueueMaxSize = 0;

    @Override
    public WriteStream<Buffer> drainHandler(final Handler<Void> handler) {

        theDrainHandler = handler;
        return this;
    }

    @Override
    public void end() {

        ended = true;

    }

    @Override
    public WriteStream<Buffer> exceptionHandler(final Handler<Throwable> handler) {

        theExceptionHandler = handler;
        return this;
    }

    public int read() {

        if (ended && data == null) {
            return -1;
        }

        if (ended && streamBufferPos == data.length()) {
            return -1;
        }

        if (data == null) {

        }

        return data.getByte(streamBufferPos++);
    }

    @Override
    public WriteStream<Buffer> setWriteQueueMaxSize(final int maxSize) {

        writeQueueMaxSize = maxSize;
        return this;
    }

    /**
     * Resets the buffer
     */
    @Override
    public WriteStream<Buffer> write(final Buffer data) {

        this.data = data;
        streamBufferPos = 0;
        return this;
    }

    @Override
    public boolean writeQueueFull() {

        return data != null;
    }

}

public class VertxInputStream extends InputStream {

    private Buffer buffer;

    private boolean ended = false;

    private final ReadStream<Buffer> readStream;

    private int streamBufferPos;

    public VertxInputStream(
        final ReadStream<Buffer> readStream) {

        this.readStream = readStream;
    }

    @Override
    public int available() throws IOException {

        if (buffer == null) {
            return 0;
        }
        return buffer.length() - streamBufferPos;
    }

    @Override
    public void close() throws IOException {

        System.out.println("CLOSE");
        readStream.pause();
    }

    public void end() {

        System.out.println("ENDED");
        ended = true;

    }

    /**
     * Pause the read stream store the buffer that was last read from the read
     * stream.
     *
     * @param buffer
     */
    public void populate(final Buffer buffer) {

        System.out.println("Populate " + buffer);
        readStream.pause();
        this.buffer = buffer;
        streamBufferPos = 0;

    }

    @Override
    public int read() throws IOException {

        if (ended && streamBufferPos == buffer.length()) {
            return -1;
        }

        if (buffer == null) {
            System.out.println("BAD!");
            return -1;
        }
        System.out.println("p=" + streamBufferPos);
        final byte b = buffer.getByte(streamBufferPos++);
        if (streamBufferPos == buffer.length()) {
            System.out.println("resuming!");
            readStream.resume();
        }
        return b;
    }

}
