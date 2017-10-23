package net.trajano.ms.engine.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.impl.CookieImpl;
import net.trajano.ms.engine.internal.resteasy.CaseInsenstiveMultivalueStringMap;

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

    /**
     * Converts a Vert.X cookie to a JAX-RS Cookie.
     *
     * @param vertxCookie
     *            Vert.X cookie
     * @return JAX-RS Cookie
     */
    public static Cookie toCookie(final io.vertx.ext.web.Cookie vertxCookie) {

        return new Cookie(vertxCookie.getName(), vertxCookie.getValue(), vertxCookie.getPath(), vertxCookie.getDomain());
    }

    public static Map<String, Cookie> toCookies(final Set<io.vertx.ext.web.Cookie> vertxCookies) {

        final Map<String, Cookie> cookies = new HashMap<>(vertxCookies.size());
        vertxCookies.forEach(vertxCookie -> cookies.put(vertxCookie.getName(), toCookie(vertxCookie)));
        return cookies;
    }

    public static MultivaluedMap<String, Object> toMultivaluedMap(final MultiMap multimap) {

        final MultivaluedMap<String, Object> mvm = new MultivaluedHashMap<>(multimap.size());
        multimap.forEach(entry -> mvm.add(entry.getKey(), entry.getValue()));
        return mvm;
    }

    /**
     * This returns a case-insensitive multivalued map suitable for headers.
     *
     * @param multimap
     * @return
     */
    public static MultivaluedMap<String, String> toMultivaluedStringMap(final MultiMap multimap) {

        final MultivaluedMap<String, String> mvm = new CaseInsenstiveMultivalueStringMap();
        multimap.forEach(entry -> mvm.add(entry.getKey(), entry.getValue()));
        return mvm;
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

    public static io.vertx.ext.web.Cookie toVertxCookie(final NewCookie cookie) {

        final io.vertx.ext.web.Cookie vertxCookie = new CookieImpl(cookie.getName(), cookie.getValue());
        vertxCookie.setPath(cookie.getPath());
        vertxCookie.setDomain(cookie.getDomain());
        vertxCookie.setHttpOnly(cookie.isHttpOnly());
        vertxCookie.setChanged(true);
        vertxCookie.setMaxAge(cookie.getMaxAge());
        vertxCookie.setSecure(cookie.isSecure());
        return vertxCookie;
    }

    private Conversions() {

    }
}
