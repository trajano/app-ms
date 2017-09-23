package net.trajano.ms.engine.sample;

import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;

public class Main extends AbstractVerticle {

    public static void main(final String[] args) {

        //        final VertxOptions options = new VertxOptions();
        //        Vertx.clusteredVertx(options, event -> {
        //            final Vertx vertx = event.result();
        //            vertx.deployVerticle(new Main());
        //
        //        });

        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Main());

    }

    @Override
    public void start() throws Exception {

        final HttpServer server = vertx.createHttpServer();

        final VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        System.out.println(deployment.getRegistry());
        deployment.setApplicationClass(MyApp.class.getName());

        //        final VertxRegistry vertxRegistry = new VertxRegistry(new ResourceMethodRegistry(deployment.getProviderFactory()));
        //        vertxRegistry.addPerRequestResource(Hello.class);
        //        deployment.setRegistry(vertxRegistry);
        //        deployment.getRegistry().addPerRequestResource(Hello.class);
        System.out.println(deployment.getActualProviderClasses());
        System.out.println(deployment.getActualResourceClasses());
        //        server.requestHandler(new VertxRequestHandler(vertx, deployment, "/", new SimpleSecurityDomain()))
        //            .listen(8280);
        //        final Router router = Router.router(vertx);
        //
        //        JaxRsRoute.route(vertx, router, MyApp.class);
        //        http.requestHandler(req -> router.accept(req)).listen(8280);
    }
}
