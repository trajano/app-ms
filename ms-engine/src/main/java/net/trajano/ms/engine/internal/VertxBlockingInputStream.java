package net.trajano.ms.engine.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

public class VertxBlockingInputStream extends InputStream {

    private static final Buffer END_BUFFER = new Buffer() {

        @Override
        public Buffer appendBuffer(final Buffer buff) {

            return null;
        }

        @Override
        public Buffer appendBuffer(final Buffer buff,
            final int offset,
            final int len) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendByte(final byte b) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendBytes(final byte[] bytes) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendBytes(final byte[] bytes,
            final int offset,
            final int len) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendDouble(final double d) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendFloat(final float f) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendInt(final int i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendIntLE(final int i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendLong(final long l) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendLongLE(final long l) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendMedium(final int i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendMediumLE(final int i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendShort(final short s) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendShortLE(final short s) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendString(final String str) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendString(final String str,
            final String enc) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendUnsignedByte(final short b) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendUnsignedInt(final long i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendUnsignedIntLE(final long i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendUnsignedShort(final int s) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer appendUnsignedShortLE(final int s) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer copy() {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer getBuffer(final int start,
            final int end) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte getByte(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public ByteBuf getByteBuf() {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getBytes() {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer getBytes(final byte[] dst) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer getBytes(final byte[] dst,
            final int dstIndex) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getBytes(final int start,
            final int end) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer getBytes(final int start,
            final int end,
            final byte[] dst) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer getBytes(final int start,
            final int end,
            final byte[] dst,
            final int dstIndex) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public double getDouble(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public float getFloat(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getInt(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getIntLE(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getLong(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getLongLE(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getMedium(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getMediumLE(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public short getShort(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public short getShortLE(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getString(final int start,
            final int end) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getString(final int start,
            final int end,
            final String enc) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public short getUnsignedByte(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getUnsignedInt(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getUnsignedIntLE(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getUnsignedMedium(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getUnsignedMediumLE(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getUnsignedShort(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getUnsignedShortLE(final int pos) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int length() {

            return 0;
        }

        @Override
        public int readFromBuffer(final int pos,
            final Buffer buffer) {

            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Buffer setBuffer(final int pos,
            final Buffer b) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setBuffer(final int pos,
            final Buffer b,
            final int offset,
            final int len) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setByte(final int pos,
            final byte b) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setBytes(final int pos,
            final byte[] b) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setBytes(final int pos,
            final byte[] b,
            final int offset,
            final int len) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setBytes(final int pos,
            final ByteBuffer b) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setDouble(final int pos,
            final double d) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setFloat(final int pos,
            final float f) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setInt(final int pos,
            final int i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setIntLE(final int pos,
            final int i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setLong(final int pos,
            final long l) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setLongLE(final int pos,
            final long l) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setMedium(final int pos,
            final int i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setMediumLE(final int pos,
            final int i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setShort(final int pos,
            final short s) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setShortLE(final int pos,
            final short s) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setString(final int pos,
            final String str) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setString(final int pos,
            final String str,
            final String enc) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setUnsignedByte(final int pos,
            final short b) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setUnsignedInt(final int pos,
            final long i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setUnsignedIntLE(final int pos,
            final long i) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setUnsignedShort(final int pos,
            final int s) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer setUnsignedShortLE(final int pos,
            final int s) {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer slice() {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer slice(final int start,
            final int end) {

            return null;
        }

        @Override
        public JsonArray toJsonArray() {

            return null;
        }

        @Override
        public JsonObject toJsonObject() {

            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String toString(final Charset enc) {

            return toString();
        }

        @Override
        public String toString(final String enc) {

            return toString();
        }

        @Override
        public void writeToBuffer(final Buffer buffer) {

            throw new UnsupportedOperationException();
        }

    };

    private Buffer currentBuffer;

    private int pos;

    final BlockingQueue<Buffer> queue = new LinkedBlockingQueue<>();

    final ReadStream<Buffer> readStream;

    public VertxBlockingInputStream(final ReadStream<Buffer> readStream) {

        this.readStream = readStream;
    }

    public void end() {

        queue.add(END_BUFFER);
    }

    public void populate(final Buffer buffer) {

        queue.add(buffer);

    }

    @Override
    public int read() throws IOException {

        if (currentBuffer == null) {
            try {
                queue.take();
                pos = 0;
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (currentBuffer == END_BUFFER) {
            return -1;
        }
        final byte b = currentBuffer.getByte(pos++);
        if (pos == currentBuffer.length()) {
            currentBuffer = null;
        }
        return b;
    }

}
