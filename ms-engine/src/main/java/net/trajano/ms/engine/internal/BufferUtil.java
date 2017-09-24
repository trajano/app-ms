package net.trajano.ms.engine.internal;

import java.io.IOException;
import java.io.InputStream;

import io.vertx.core.buffer.Buffer;

public final class BufferUtil {

    public static Buffer bufferFromClasspathResource(final String path) throws IOException {

        final Buffer buffer = Buffer.buffer();

        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        final byte[] buf = new byte[1024];
        int nRead;
        while ((nRead = is.read(buf, 0, buf.length)) != -1) {
            buffer.appendBytes(buf, 0, nRead);
        }
        return buffer;
    }

}
