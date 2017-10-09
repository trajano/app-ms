package net.trajano.ms.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

@Component
public class GatewayVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayVerticle.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private HttpServer httpServer;

    @Autowired
    private Router router;

    @Override
    public void start() throws Exception {

        httpServer.requestHandler(router::accept).listen(res -> {
            if (res.failed()) {
                LOG.error(res.cause().getMessage(), res.cause());
                SpringApplication.exit(applicationContext, () -> -1);
            } else {
                LOG.info("Listening on port {}", httpServer.actualPort());
            }
        });
    }
}
