package net.trajano.ms.engine.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import io.vertx.core.buffer.Buffer;

public final class BufferUtil {

    public static Buffer bufferFromClasspathResource(final String path) {

        final Buffer buffer = Buffer.buffer();

        try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            final byte[] buf = new byte[1024];
            int nRead;
            while ((nRead = is.read(buf, 0, buf.length)) != -1) {
                buffer.appendBytes(buf, 0, nRead);
            }
            return buffer;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private BufferUtil() {

    }

}
