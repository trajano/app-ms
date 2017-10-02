package net.trajano.ms.engine.internal.resteasy;

import java.net.URI;
import java.util.stream.Collectors;

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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class VertxRequestHandler implements
    Handler<RoutingContext>,
    AutoCloseable {

    //    private final AnnotationConfigApplicationContext applicationContext;

    private final ResteasyDeployment deployment;

    private final SynchronousDispatcher dispatcher;

    private final ResteasyProviderFactory providerFactory;

    public VertxRequestHandler(final AnnotationConfigApplicationContext applicationContext,
        final Class<? extends Application> applicationClass) {

        deployment = new ResteasyDeployment();
        deployment.setApplicationClass(applicationClass.getName());
        final Reflections reflections = new Reflections(applicationClass.getPackage().getName());
        deployment.setScannedResourceClasses(reflections.getTypesAnnotatedWith(Path.class).stream().map(clazz -> clazz.getName()).collect(Collectors.toList()));
        deployment.setScannedProviderClasses(reflections.getTypesAnnotatedWith(Provider.class).stream().map(clazz -> clazz.getName()).collect(Collectors.toList()));
        deployment.start();

        //        this.applicationContext = applicationContext;
        providerFactory = deployment.getProviderFactory();
        dispatcher = (SynchronousDispatcher) deployment.getDispatcher();

        //        dispatcher.getRegistry().addPerRequestResource(Hello.class);
        final SpringBeanProcessor springBeanProcessor = new SpringBeanProcessor(dispatcher);
        applicationContext.addBeanFactoryPostProcessor(springBeanProcessor);
        applicationContext.addApplicationListener(springBeanProcessor);
    }

    @Override
    public void close() {

        deployment.stop();

    }

    @Override
    public void handle(final RoutingContext context) {

        context.vertx().executeBlocking(
            future -> {
                try {
                    ThreadLocalResteasyProviderFactory.push(providerFactory);
                    try {
                        ResteasyProviderFactory.pushContext(RoutingContext.class, context);
                        dispatcher.invokePropagateNotFound(new VertxHttpRequest(context,
                            URI.create(deployment.getApplication().getClass().getAnnotation(ApplicationPath.class).value()), dispatcher),
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
