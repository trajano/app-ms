package net.trajano.ms.engine.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.JaxRsRoute;
import net.trajano.ms.engine.JaxRsVerticle;

public class Main extends AbstractVerticle {

    public static void main(final String[] args) {

        //        final VertxOptions options = new VertxOptions();
        //        Vertx.clusteredVertx(options, event -> {
        //            final Vertx vertx = event.result();
        //            vertx.deployVerticle(new Main());
        //
        //        });

        final Vertx vertx = Vertx.vertx();
        final DeploymentOptions options = new DeploymentOptions();
        vertx.deployVerticle(JaxRsVerticle.class.getName(), options);

    }

    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);
        final HttpServer http = vertx.createHttpServer();

        JaxRsRoute.route(vertx, router, MyApp.class);
        http.requestHandler(req -> router.accept(req)).listen(8280);
    }
}