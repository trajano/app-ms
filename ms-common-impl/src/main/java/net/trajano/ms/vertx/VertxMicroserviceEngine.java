package net.trajano.ms.vertx;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.Microservice;
import net.trajano.ms.engine.ManifestHandler;
import net.trajano.ms.engine.SpringJaxRsHandler;
import net.trajano.ms.engine.SwaggerHandler;
import net.trajano.ms.spi.MicroserviceEngine;
import net.trajano.ms.vertx.beans.GsonJacksonJsonOps;
import net.trajano.ms.vertx.beans.GsonProvider;
import net.trajano.ms.vertx.beans.JcaCryptoOps;
import net.trajano.ms.vertx.beans.JwksProvider;
import net.trajano.ms.vertx.beans.JwksRouteHandler;
import net.trajano.ms.vertx.jaxrs.CommonMsJaxRs;

@Component
public class VertxMicroserviceEngine implements
    MicroserviceEngine {

    private static final Logger LOG = LoggerFactory.getLogger(VertxMicroserviceEngine.class);

    @Autowired
    private ConfigurableApplicationContext baseApplicationContext;

    private final Deque<AutoCloseable> handlerStack = new LinkedList<>();

    @Autowired
    private HttpServerOptions httpServerOptions;

    private Vertx vertx;

    @Autowired
    private VertxOptions vertxOptions;

    /**
     * Sets the system properties and sets up the logger. {@inheritDoc}
     */
    @Override
    public Object[] bootstrap() {

        System.setProperty("vertx.disableDnsResolver", "true");
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

        final File logbackFile = new File("logback.xml");
        if (logbackFile.exists()) {
            System.setProperty("logging.config", logbackFile.getAbsolutePath());
        }

        return new Object[] {
            VertxConfig.class,
            VertxMicroserviceEngine.class
        };
    }

    @PostConstruct
    public void start() {

        LOG.debug("Application={}", Microservice.getApplicationClass());

        if (Microservice.getApplicationClass() == null) {
            LOG.warn("No application class specified, assuming running as a unit test");
            return;
        }
        vertx = Vertx.vertx(vertxOptions);
        final Router router = Router.router(vertx);

        handlerStack.push(SwaggerHandler.registerToRouter(router, Microservice.getApplicationClass()));
        handlerStack.push(ManifestHandler.registerToRouter(router));

        final Handler<RoutingContext> notFoundHandler = ctx -> ctx.response().setStatusCode(404).setStatusMessage(Status.NOT_FOUND.getReasonPhrase()).end(Status.NOT_FOUND.getReasonPhrase());
        router.get("/favicon.ico").handler(notFoundHandler);

        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setParent(baseApplicationContext);
        applicationContext.register(GsonJacksonJsonOps.class, GsonProvider.class, JcaCryptoOps.class, JwksProvider.class, JwksRouteHandler.class);
        applicationContext.register(CommonMsJaxRs.class);
        handlerStack.push(SpringJaxRsHandler.registerToRouter(router, applicationContext, Microservice.getApplicationClass()));

        final JwksRouteHandler jwksRouteHandler = applicationContext.getBean(JwksRouteHandler.class);
        // Prioritize JWKS higher than default router.
        router.route("/.well-known/jwks").order(-1).handler(jwksRouteHandler);

        final HttpServer http = vertx.createHttpServer(httpServerOptions);

        http.requestHandler(router::accept).listen(res -> {
            if (res.failed()) {
                LOG.error(res.cause().getMessage(), res.cause());
                SpringApplication.exit(baseApplicationContext, () -> -1);
            } else {
                LOG.info("Listening on port {}", http.actualPort());
            }
        });
    }

    @PreDestroy
    public void stop() throws Exception {

        while (handlerStack.peek() != null) {
            handlerStack.pop().close();
        }
        vertx.close();
    }

}
