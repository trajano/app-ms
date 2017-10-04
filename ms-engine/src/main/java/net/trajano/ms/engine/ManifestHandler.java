package net.trajano.ms.engine;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.jar.Manifest;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.BufferUtil;
import net.trajano.ms.engine.internal.VertxBufferInputStream;

/**
 * This creates a route that will provide data from the MANIFIEST.MF of the
 * application which can contain information such as version and build number.
 *
 * @author Archimedes Trajano
 */
public class ManifestHandler implements
    Handler<RoutingContext>,
    AutoCloseable {

    /**
     * Constructs a new route for the given router to a JAX-RS application to
     * ".well-known/manifest"
     *
     * @param router
     */
    public static ManifestHandler registerToRouter(
        final Router router) {

        return registerToRouter(router, "/.well-known/manifest");
    }

    /**
     * Constructs a new route for the given router to a JAX-RS application.
     *
     * @param router
     *            Vert.x router
     * @param uri
     *            uri that the route would be mapped to
     */
    public static ManifestHandler registerToRouter(
        final Router router,
        final String uri) {

        final ManifestHandler requestHandler = new ManifestHandler();
        router.get(uri).handler(requestHandler);
        return requestHandler;
    }

    private final Buffer json;

    private final Buffer text;

    public ManifestHandler() {

        try {
            text = BufferUtil.bufferFromClasspathResource("META-INF/MANIFEST.MF");
            Manifest mf;
            mf = new Manifest(new VertxBufferInputStream(text));
            final JsonObject mapFrom = JsonObject.mapFrom(mf.getMainAttributes());
            json = mapFrom.toBuffer();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public void close() throws Exception {

        // does nothing

    }

    @Override
    public void handle(final RoutingContext context) {

        if (TEXT_PLAIN.equals(context.getAcceptableContentType())) {
            context.response().putHeader(CONTENT_TYPE, TEXT_PLAIN).end(text);
        } else {
            context.response().putHeader(CONTENT_TYPE, APPLICATION_JSON).end(json);
        }

    }
}
