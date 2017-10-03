package net.trajano.ms.engine;

import java.net.URI;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.ThreadLocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.reflections.Reflections;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.SpringConfiguration;
import net.trajano.ms.engine.internal.VertxRequestContextFilter;
import net.trajano.ms.engine.internal.resteasy.VertxHttpRequest;
import net.trajano.ms.engine.internal.resteasy.VertxHttpResponse;

public class SwaggerHandler implements
    Handler<RoutingContext>,
    AutoCloseable {

    //    private final AnnotationConfigApplicationContext applicationContext;

    private final AnnotationConfigApplicationContext applicationContext;

    private final ResteasyDeployment deployment;

    private final SynchronousDispatcher dispatcher;

    private final ResteasyProviderFactory providerFactory;

    public SwaggerHandler(
        final Class<? extends Application> applicationClass) {

        this(new StaticApplicationContext(), applicationClass);
    }

    public SwaggerHandler(final ConfigurableApplicationContext baseApplicationContext,
        final Class<? extends Application> applicationClass) {

        baseApplicationContext.refresh();
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setParent(baseApplicationContext);
        applicationContext.register(SpringConfiguration.class, applicationClass, VertxRequestContextFilter.class);

        final Reflections reflections = new Reflections(applicationClass.getPackage().getName());

        reflections.getTypesAnnotatedWith(Path.class).forEach(clazz -> applicationContext.register(clazz));
        reflections.getTypesAnnotatedWith(Provider.class).forEach(clazz -> applicationContext.register(clazz));

        deployment = new ResteasyDeployment();
        deployment.setApplicationClass(applicationClass.getName());
        deployment.start();

        //        dispatcher.getRegistry().addPerRequestResource(Hello.class);
        final SpringBeanProcessor springBeanProcessor = new SpringBeanProcessor(deployment);

        applicationContext.addBeanFactoryPostProcessor(springBeanProcessor);
        applicationContext.addApplicationListener(springBeanProcessor);
        applicationContext.refresh();
        dispatcher = (SynchronousDispatcher) deployment.getDispatcher();
        providerFactory = deployment.getProviderFactory();
    }

    @Override
    public void close() {

        deployment.stop();
        applicationContext.close();

    }

    @Override
    public void handle(final RoutingContext context) {

        context.vertx().executeBlocking(
            future -> {
                try {
                    ThreadLocalResteasyProviderFactory.push(providerFactory);
                    try {
                        ResteasyProviderFactory.pushContext(RoutingContext.class, context);
                        ResteasyProviderFactory.pushContext(Vertx.class, context.vertx());

                        //                      applicationContext.getBeansWithAnnotation(Path.class).forEach((name,
                        //                      object) -> {
                        //                      deployment.getRegistry().removeRegistrations(object.getClass());
                        //                      deployment.getRegistry().addSingletonResource(object);
                        //                  });
                        //
                        //                  applicationContext.getBeansWithAnnotation(Provider.class).forEach((name,
                        //                      object) -> {
                        //                      deployment.getProviderFactory().registerProviderInstance(object);
                        //                  });

                        final Application application = deployment.getApplication();
                        dispatcher.invokePropagateNotFound(new VertxHttpRequest(context,
                            URI.create(application.getClass().getAnnotation(ApplicationPath.class).value()), dispatcher),
                            new VertxHttpResponse(context));
                        context.response().end();
                    } finally {
                        ResteasyProviderFactory.clearContextData();
                    }
                } finally {
                    ThreadLocalResteasyProviderFactory.pop();
                }
            },
            res -> {
                if (res.failed()) {
                    if (res.cause() instanceof NotFoundException) {
                        context.response().setStatusCode(404);
                        context.response().setStatusMessage(Status.NOT_FOUND.getReasonPhrase());
                        if (context.request().method() != HttpMethod.HEAD) {
                            context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
                            context.response().end(res.cause().getLocalizedMessage());
                        }
                    } else {
                        System.err.println("XXX");
                        res.cause().printStackTrace();
                        System.err.println("xxx");
                        context.response().setStatusCode(500);
                        context.response().setStatusMessage(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                        context.response().end();
                    }
                }
            });
    }

}
