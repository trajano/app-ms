package net.trajano.ms.engine.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.internal.resteasy.VertxRequestHandler;

public class Main2 extends AbstractVerticle {

    public static void main(final String[] args) {

        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        final VertxOptions vertOptions = new VertxOptions();
        //        vertOptions.setMaxEventLoopExecuteTime(4000000000L);
        vertOptions.setWarningExceptionTime(1);
        vertOptions.setWorkerPoolSize(50);
        //        final VertxOptions options = new VertxOptions();
        //        Vertx.clusteredVertx(options, event -> {
        //            final Vertx vertx = event.result();
        //            vertx.deployVerticle(new Main());
        //
        //        });

        final Vertx vertx = Vertx.vertx(vertOptions);
        final DeploymentOptions options = new DeploymentOptions();
        vertx.deployVerticle(new Main2(), options);
    }

    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);

        //final HttpServerOptions httpServerOptions = new HttpServerOptions();

        final HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setPort(8900);
        final HttpServer http = vertx.createHttpServer(httpServerOptions);

        try (final VertxRequestHandler requestHandler = new VertxRequestHandler(MyApp.class)) {
            router.route("/api/*")
                .useNormalisedPath(true)
                .handler(requestHandler);

            http.requestHandler(req -> router.accept(req)).listen(res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                    vertx.close();
                }
            });
        }
    }

}
