package net.trajano.ms.engine;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.internal.BufferUtil;
import net.trajano.ms.engine.sample.MyApp;
import net.trajano.ms.engine.second.MyApp2;

/**
 * This vericle provdes the HTTP server and {@link Router} that is going to be
 * used by {@link JaxRsRoute}
 *
 * @author Archimedes Trajano
 */
public class JaxRsVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        final ConfigRetriever retriever = ConfigRetriever.create(vertx);

        final Router router = Router.router(vertx);
        final Buffer cert = BufferUtil.bufferFromClasspathResource("cert.pem");
        final Buffer key = BufferUtil.bufferFromClasspathResource("key.pem");
        final HttpServerOptions httpServerOptions = new HttpServerOptions()
            .setUseAlpn(true)
            .setSsl(true)
            .setKeyCertOptions(new PemKeyCertOptions().addCertValue(cert).addKeyValue(key));

        final HttpServer http = vertx.createHttpServer(httpServerOptions);

        final Integer port = 8280;// config().getJsonObject("http").getInteger("port", 8280);

        JaxRsRoute.route(vertx, router, MyApp.class);
        JaxRsRoute.route(vertx, router, MyApp2.class);
        http.requestHandler(req -> router.accept(req)).listen(port);

    }

}
