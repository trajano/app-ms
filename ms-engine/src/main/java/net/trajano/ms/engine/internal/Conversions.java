package net.trajano.ms.engine.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.impl.CookieImpl;

public final class Conversions {

    /**
     * Converts a VertX cookie to a JaxRS Cookie.
     *
     * @param vertxCookie
     * @return
     */
    public static Cookie toCookie(final io.vertx.ext.web.Cookie vertxCookie) {

        return new Cookie(vertxCookie.getName(), vertxCookie.getValue(), vertxCookie.getPath(), vertxCookie.getDomain());
    }

    public static Map<String, Cookie> toCookies(final Set<io.vertx.ext.web.Cookie> vertxCookies) {

        final Map<String, Cookie> cookies = new HashMap<>(vertxCookies.size());
        vertxCookies.forEach(vertxCookie -> {
            cookies.put(vertxCookie.getName(), toCookie(vertxCookie));
        });
        return cookies;
    }

    public static MultivaluedMap<String, Object> toMultivaluedMap(final MultiMap multimap) {

        final MultivaluedMap<String, Object> mvm = new MultivaluedHashMap<>(multimap.size());
        multimap.forEach(entry -> {
            mvm.add(entry.getKey(), entry.getValue());
        });
        return mvm;
    }

    public static MultivaluedMap<String, String> toMultivaluedStringMap(final MultiMap multimap) {

        final MultivaluedMap<String, String> mvm = new MultivaluedHashMap<>(multimap.size());
        multimap.forEach(entry -> {
            mvm.add(entry.getKey(), entry.getValue());
        });
        return mvm;
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
}
