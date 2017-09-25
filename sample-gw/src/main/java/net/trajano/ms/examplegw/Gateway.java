package net.trajano.ms.examplegw;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.RequestOptions;
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

        router.routeWithRegex("/v1/.*").handler(new ProxyRouteHandler(client, req -> {
            final RequestOptions clientRequestOptions = new RequestOptions();
            clientRequestOptions.setHost("localhost");
            clientRequestOptions.setPort(8080);
            clientRequestOptions.setURI(req.uri().substring(3));
            return clientRequestOptions;
        }));

        vertx.createHttpServer()
            .requestHandler(req -> {
                router.accept(req);
            })
            .listen(8180);
    }
}
