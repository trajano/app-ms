package net.trajano.ms.common;

import java.util.Deque;
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import net.trajano.ms.common.beans.CommonMs;
import net.trajano.ms.common.beans.JwksRouteHandler;
import net.trajano.ms.common.internal.config.ConfigurationProvider;
import net.trajano.ms.common.jaxrs.CommonMsJaxRs;
import net.trajano.ms.engine.ManifestHandler;
import net.trajano.ms.engine.SpringJaxRsHandler;
import net.trajano.ms.engine.SwaggerHandler;

@Component
public class Microservice {

    private static Class<? extends Application> applicationClass;

    private static final Logger LOG = LoggerFactory.getLogger(Microservice.class);

    /**
     * @param applicationClass
     *            JAX-RS Application class
     * @param args
     *            command line arguments
     * @throws Exception
     */
    public static void run(final Class<? extends Application> applicationClass,
        final String... args) throws Exception {

        if (Microservice.applicationClass != null) {
            throw new IllegalStateException("Another Application class has already been registered in this JVM.");
        }
        Microservice.applicationClass = applicationClass;
        final Object[] sources = new Object[] {
            ConfigurationProvider.class,
            Microservice.class
        };
        final SpringApplication springApplication = new SpringApplication(sources);
        springApplication
            .setBannerMode(Mode.OFF);
        springApplication.run(args);
    }

    @Autowired
    private ConfigurableApplicationContext baseApplicationContext;

    private final Deque<AutoCloseable> handlerStack = new LinkedList<>();

    @Autowired
    private HttpServerOptions httpServerOptions;

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

        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setParent(baseApplicationContext);
        applicationContext.register(CommonMs.class);
        applicationContext.register(CommonMsJaxRs.class);
        handlerStack.push(SpringJaxRsHandler.registerToRouter(router, applicationContext, applicationClass));

        final JwksRouteHandler jwksRouteHandler = applicationContext.getBean(JwksRouteHandler.class);
        router.route("/.well-known/jwks").handler(jwksRouteHandler);

        final HttpServer http = vertx.createHttpServer(httpServerOptions);

        http.requestHandler(req -> router.accept(req)).listen(res -> {
            if (res.failed()) {
                LOG.error(res.cause().getMessage(), res.cause());
                vertx.close();
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
