package net.trajano.ms.common;

import java.util.Stack;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.ManifestHandler;
import net.trajano.ms.engine.SpringJaxRsHandler;
import net.trajano.ms.engine.SwaggerHandler;

@Component
public class MicroserviceVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(SpringJaxRsHandler.class);

    private final Stack<AutoCloseable> handlerStack = new Stack<>();

    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);

        @SuppressWarnings("unchecked")
        final Class<? extends Application> applicationClass = (Class<? extends Application>) Class.forName(config().getString("application_class"));
        LOG.debug("Application={}", applicationClass.getName());

        handlerStack.push(SwaggerHandler.registerToRouter(router, applicationClass));
        handlerStack.push(ManifestHandler.registerToRouter(router));
        handlerStack.push(SpringJaxRsHandler.registerToRouter(router, applicationClass));

        final HttpServerOptions httpServerOptions = new HttpServerOptions(config().getJsonObject("http"));

        final HttpServer http = vertx.createHttpServer(httpServerOptions);

        http.requestHandler(req -> router.accept(req)).listen(res -> {
            if (res.failed()) {
                LOG.error(res.cause().getMessage(), res.cause());
                vertx.close();
            } else {
                LOG.debug("Listening on port {}", http.actualPort());
            }
        });
    }

    @Override
    public void stop() throws Exception {

        while (handlerStack.peek() != null) {
            handlerStack.pop().close();
        }
    }

}
