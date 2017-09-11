package net.trajano.ms.examplegw;

import io.vertx.core.Vertx;

public class Gateway {

    public static void main(final String[] args) {

        // Create an HTTP server which simply returns "Hello World!" to each request.
        Vertx.vertx()
            .createHttpServer()
            .requestHandler(req -> req.response().end("Hello World!")).listen(8383);
    }
}
