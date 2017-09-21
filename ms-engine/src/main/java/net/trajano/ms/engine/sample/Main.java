package net.trajano.ms.engine.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.JaxRsRoute;

public class Main extends AbstractVerticle {

    public static void main(final String[] args) {

        final VertxOptions options = new VertxOptions();
        Vertx.clusteredVertx(options, event -> {
            final Vertx vertx = event.result();
            vertx.deployVerticle(Main.class.getName());

            final JsonObject config = new JsonObject().put("application_class", MyApp.class.getName()).put("directory", "/blah");
            final DeploymentOptions jaxRsOptions = new DeploymentOptions().setConfig(config);
            vertx.deployVerticle(JaxRsRoute.class.getName(), jaxRsOptions);

        });

        //final Vertx vertx = Vertx.vertx();

    }

    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);
        final HttpServer http = vertx.createHttpServer();

        JaxRsRoute.route(vertx, router, MyApp.class);
        http.requestHandler(req -> router.accept(req)).listen(8280);
    }
}
