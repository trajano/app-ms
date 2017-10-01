package net.trajano.ms.common;

import javax.ws.rs.core.Application;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.JaxRsRoute;
import net.trajano.ms.engine.ManifestRoute;

public class MsEngineApplication {

    public static void run(final Class<? extends Application> application) {

        run(Vertx.vertx(), application);

    }

    public static void run(final Vertx vertx,
        final Class<? extends Application> applicationClass) {

        final ConfigStoreOptions yamlStore = new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject()
                .put("path", ".")
                .put("filesets", new JsonArray()
                    .add(new JsonObject()
                        .put("pattern", "application.yml")
                        .put("format", "yaml"))));

        final ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
            .addStore(yamlStore);
        final ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);

        final Context context = vertx.getOrCreateContext();

        configRetriever.getConfig(r -> {
            if (r.succeeded()) {
                final HttpServer http = startServer(vertx, r.result(), applicationClass);
                context.put("http", http);
            }
        });
        configRetriever.listen(event -> {
            final HttpServer http = (HttpServer) context.get("http");

            http.close((v) -> {
                final HttpServer newHttp = startServer(vertx, event.getNewConfiguration(), applicationClass);
                context.put("http", newHttp);
            });
        });

    }

    private static HttpServer startServer(final Vertx vertx,
        final JsonObject config,
        final Class<? extends Application> applicationClass) {

        final Router router = Router.router(vertx);

        System.out.println("here" + config.getJsonObject("http"));

        final HttpServerOptions httpServerOptions;
        if (config.getJsonObject("http") != null) {
            httpServerOptions = new HttpServerOptions(config.getJsonObject("http"));
        } else {
            httpServerOptions = new HttpServerOptions().setPort(8210);
        }
        System.out.println("here ok" + httpServerOptions.getPort());
        if (config.getString("certificatePath") != null && config.getString("keyPath") != null) {
            httpServerOptions
                .setKeyCertOptions(new PemKeyCertOptions()
                    .addCertPath(config.getString("certificatePath"))
                    .addKeyPath(config.getString("keyPath")));
        }

        System.out.println("here");
        final HttpServer http = vertx.createHttpServer(httpServerOptions);
        System.out.println("http" + http);
        JaxRsRoute.route(router, applicationClass);
        System.out.println("here before route");
        ManifestRoute.route(router, "/info");
        http.requestHandler(req -> router.accept(req)).listen(res -> {
            if (res.failed()) {
                res.cause().printStackTrace();
            } else {
                System.out.println("Here route");
                System.out.println(http.actualPort());
            }
        });
        System.out.println("here");
        return http;

    }
}
