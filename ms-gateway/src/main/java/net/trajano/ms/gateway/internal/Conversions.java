package net.trajano.ms.gateway.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.RequestOptions;

public final class Conversions {

    /**
     * Pumps the contents of an {@link InputStream} to a {@link Buffer}.
     *
     * @param is
     *            input stream
     * @return buffer
     */
    public static Buffer toBuffer(final InputStream is) throws IOException {

        final Buffer b = Buffer.buffer(1024);
        final byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) >= 0) {
            b.appendBytes(buf, 0, len);
        }
        return b;

    }

    public static RequestOptions toRequestOptions(final URI uri) {

        final RequestOptions options = new RequestOptions()
            .setSsl("https".equals(uri.getScheme()))
            .setHost(uri.getHost());
        if (uri.getPort() > 0) {
            options.setPort(uri.getPort());
        } else if (options.isSsl()) {
            options.setPort(443);
        } else {
            options.setPort(80);
        }
        if (uri.getRawQuery() == null) {
            options.setURI(uri.getRawPath());
        } else {
            options.setURI(uri.getRawPath() + "?" + uri.getRawQuery());
        }
        return options;
    }
}
