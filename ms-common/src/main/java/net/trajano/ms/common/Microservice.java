package net.trajano.ms.common;

import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import net.trajano.ms.common.internal.JwksRouteHandler;
import net.trajano.ms.engine.ManifestHandler;
import net.trajano.ms.engine.SpringJaxRsHandler;
import net.trajano.ms.engine.SwaggerHandler;

@ComponentScan
@EnableScheduling
@Configuration
public class Microservice {

    private static Class<? extends Application> applicationClass;

    private static final Logger LOG = LoggerFactory.getLogger(Microservice.class);

    public static void run(final Class<? extends Application> applicationClass,
        final String... args) throws Exception {

        if (Microservice.applicationClass != null) {
            throw new IllegalStateException("Another Application class has already been registered in this JVM.");
        }
        Microservice.applicationClass = applicationClass;
        final Object[] sources = new Object[] {
            Microservice.class
        };
        final SpringApplication springApplication = new SpringApplication(sources);
        springApplication
            .setBannerMode(Mode.OFF);
        springApplication.run(args);
    }

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private final Stack<AutoCloseable> handlerStack = new Stack<>();

    @Autowired
    private HttpServerOptions httpServerOptions;

    @Autowired
    private JwksRouteHandler jwksRouteHandler;

    private Vertx vertx;

    @Autowired
    private VertxOptions vertxOptions;

    @PostConstruct
    public void start() throws Exception {

        LOG.debug("Application={}", Microservice.applicationClass.getName());

        vertx = Vertx.vertx(vertxOptions);
        final Router router = Router.router(vertx);

        handlerStack.push(SwaggerHandler.registerToRouter(router, applicationClass));
        handlerStack.push(ManifestHandler.registerToRouter(router));
        router.route("/.well-known/jwks").handler(jwksRouteHandler);
        handlerStack.push(SpringJaxRsHandler.registerToRouter(router, applicationContext, applicationClass));

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

    @PreDestroy
    public void stop() throws Exception {

        while (handlerStack.peek() != null) {
            handlerStack.pop().close();
        }
        vertx.close();
    }
}
