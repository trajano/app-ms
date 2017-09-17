package net.trajano.ms.examplegw;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public class Gateway extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(final String[] args) {

        Vertx.vertx().deployVerticle(new Gateway());
    }

    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);
        final HttpClient client = vertx.createHttpClient(new HttpClientOptions());

        router.route("/hello").handler(event -> {

            final HttpServerResponse response = event.response();
            response.putHeader("content-type", "text/plain");
            response.end("Hello World!");
        });

        router.route().handler(event -> {
            final HttpServerRequest req = event.request();

            System.out.println("Proxying request: " + req.uri());
            final HttpClientRequest c_req = client.request(req.method(), 8080, "localhost", req.uri(), c_res -> {
                System.out.println("Proxying response: " + c_res.statusCode());
                req.response().setChunked(true);
                req.response().setStatusCode(c_res.statusCode());
                req.response().headers().setAll(c_res.headers());
                c_res.handler(data -> {
                    System.out.println("Proxying response body inside: " + data.toString("ISO-8859-1"));
                    req.response().write(data);
                });
                c_res.endHandler((v) -> req.response().end());
            });

            c_req.setChunked(true);
            c_req.headers().setAll(req.headers());
            req.handler(data -> {
                System.out.println("Proxying request body " + data.toString("ISO-8859-1"));
                c_req.write(data);
            });
            req.endHandler((v) -> c_req.end());
        });

        vertx.createHttpServer()
            .requestHandler(req -> {
                router.accept(req);
            })
            .listen(8180);
    }
}
