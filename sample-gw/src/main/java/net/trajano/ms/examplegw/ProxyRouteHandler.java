package net.trajano.ms.examplegw;

import java.util.function.Function;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.RoutingContext;

public class ProxyRouteHandler implements
    Handler<RoutingContext> {

    private final HttpClient client;

    private final Function<HttpServerRequest, RequestOptions> requestBuilder;

    public ProxyRouteHandler(final HttpClient client,
        final Function<HttpServerRequest, RequestOptions> requestBuilder) {

        this.client = client;
        this.requestBuilder = requestBuilder;
    }

    @Override
    public void handle(final RoutingContext event) {

        final HttpServerRequest req = event.request();
        final RequestOptions clientRequestOptions = requestBuilder.apply(req);
        final HttpClientRequest c_req = client.request(req.method(), clientRequestOptions, c_res -> {
            req.response().setChunked(true);
            req.response().setStatusCode(c_res.statusCode());
            req.response().headers().setAll(c_res.headers());
            c_res.handler(data -> {
                req.response().write(data);
            });
            c_res.endHandler((v) -> req.response().end());
        });

        c_req.setChunked(true);
        c_req.headers().setAll(req.headers());
        req.handler(data -> {
            c_req.write(data);
        });
        req.endHandler((v) -> c_req.end());
    }

}
