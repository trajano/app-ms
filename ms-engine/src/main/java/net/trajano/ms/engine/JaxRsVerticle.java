package net.trajano.ms.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.sample.MyApp;

/**
 * This vericle provdes the HTTP server and {@link Router} that is going to be
 * used by {@link JaxRsRoute}
 *
 * @author Archimedes Trajano
 */
public class JaxRsVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);
        final HttpServer http = vertx.createHttpServer();

        final Integer port = 8280;// config().getJsonObject("http").getInteger("port", 8280);

        JaxRsRoute.route(vertx, router, MyApp.class);
        http.requestHandler(req -> router.accept(req)).listen(port);

    }

}
