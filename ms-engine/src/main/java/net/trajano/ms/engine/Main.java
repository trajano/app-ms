package net.trajano.ms.engine;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.sample.MyApp;

public class Main {

    public static void main(final String[] args) {

        final VertxOptions options = new VertxOptions();
        //        Vertx.clusteredVertx(options, event -> {
        //            final Vertx vertx = event.result();
        //            final HttpServer http = vertx.createHttpServer();
        //            new VertxContainer(http, MyApp.class);
        //        });

        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        final HttpServer http = vertx.createHttpServer();

        VertxContainer.container(router, MyApp.class);
        http.requestHandler(req -> router.accept(req)).listen(8280);

    }
}
