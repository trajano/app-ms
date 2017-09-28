package net.trajano.ms.engine;

import javax.ws.rs.core.Application;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;

/**
 * This vericle provdes the HTTP server and {@link Router} that is going to be
 * used by {@link JaxRsRoute} Example usage: vertx run
 * net.trajano.ms.engine.JaxRsVerticle -cp ms-engine.jar;myapp.jar
 *
 * @author Archimedes Trajano
 */
public class JaxRsVerticle extends AbstractVerticle {

    @SuppressWarnings("unchecked")
    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);

        //final HttpServerOptions httpServerOptions = new HttpServerOptions();
        final HttpServerOptions httpServerOptions = new HttpServerOptions(config().getJsonObject("http"));

        //        httpServerOptions.getKeyCertOptions().
        //
        //        final HttpServerOptions httpServerOptions = config().getJsonObject("http").mapTo(HttpServerOptions.class);

        if (config().getString("certificatePath") != null && config().getString("keyPath") != null) {
            httpServerOptions
                .setKeyCertOptions(new PemKeyCertOptions()
                    .addCertPath(config().getString("certificatePath"))
                    .addKeyPath(config().getString("keyPath")));
        }

        final HttpServer http = vertx.createHttpServer(httpServerOptions);

        config().getJsonArray("applicationClasses").forEach(applicationClassName -> {
            try {
                JaxRsRoute.route(router, (Class<? extends Application>) Class.forName((String) applicationClassName));
            } catch (final ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        ManifestRoute.route(router, "/info");
        http.requestHandler(req -> router.accept(req)).listen();

    }

}
