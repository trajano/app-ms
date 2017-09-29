package net.trajano.ms.engine;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.jar.Manifest;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.internal.BufferUtil;
import net.trajano.ms.engine.internal.VertxBufferInputStream;

/**
 * This creates a route that will provide data from the MANIFIEST.MF of the
 * application which can contain information such as version and build number.
 *
 * @author Archimedes Trajano
 */
public class JwksRoute {

    /**
     * Constructs a new route for the given router to a JAX-RS application.
     *
     * @param router
     * @param uri
     *            uri that the route would be mapped to
     */
    public static void route(
        final Router router,
        final String uri) {

        try {
            final Buffer text = BufferUtil.bufferFromClasspathResource("META-INF/MANIFEST.MF");
            final Manifest mf = new Manifest(new VertxBufferInputStream(text));
            final JsonObject mapFrom = JsonObject.mapFrom(mf.getMainAttributes());
            final Buffer json = mapFrom.toBuffer();
            router.get(uri).produces(TEXT_PLAIN).handler(context -> context.response().putHeader(CONTENT_TYPE, TEXT_PLAIN).end(text));
            router.get(uri).produces(APPLICATION_JSON).handler(context -> context.response().putHeader(CONTENT_TYPE, APPLICATION_JSON).end(json));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
