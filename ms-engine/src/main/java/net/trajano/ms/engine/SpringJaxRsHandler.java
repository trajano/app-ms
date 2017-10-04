package net.trajano.ms.engine;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.ThreadLocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.SpringConfiguration;
import net.trajano.ms.engine.internal.VertxRequestContextFilter;
import net.trajano.ms.engine.internal.resteasy.VertxHttpRequest;
import net.trajano.ms.engine.internal.resteasy.VertxHttpResponse;

public class SpringJaxRsHandler implements
    Handler<RoutingContext>,
    AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SpringJaxRsHandler.class);

    /**
     * Convenience method to construct and register the routes to a Vert.x router.
     *
     * @param router
     *            vert.x router
     * @param applicationClasses
     *            application classes
     * @return the handlers
     */
    @SafeVarargs
    public static SpringJaxRsHandler[] multipleRegisterToRouter(final Router router,
        final Class<? extends Application>... applicationClasses) {

        return multipleRegisterToRouter(router, new StaticApplicationContext(), applicationClasses);
    }

    /**
     * Convenience method to construct and register the routes to a Vert.x router
     * with a base Spring application context.
     *
     * @param router
     *            vert.x router
     * @param baseApplicationContext
     *            Spring application context
     * @param applicationClasses
     *            application classes
     * @return the handlers
     */
    @SafeVarargs
    public static SpringJaxRsHandler[] multipleRegisterToRouter(final Router router,
        final ConfigurableApplicationContext baseApplicationContext,
        final Class<? extends Application>... applicationClasses) {

        final SpringJaxRsHandler[] ret = new SpringJaxRsHandler[applicationClasses.length];
        int i = 0;
        for (final Class<? extends Application> applicationClass : applicationClasses) {
            final SpringJaxRsHandler requestHandler = new SpringJaxRsHandler(baseApplicationContext, applicationClass);
            router.route(requestHandler.baseUriRoute())
                .useNormalisedPath(true)
                .handler(requestHandler);
            ret[i++] = requestHandler;
            LOG.debug("Route to {} handled by {}", requestHandler.baseUriRoute(), requestHandler);
        }
        return ret;

    }

    /**
     * Convenience method to construct and register a single application route to a
     * Vert.x router.
     *
     * @param router
     *            vert.x router
     * @param applicationClass
     *            application class
     * @return the handler
     */
    public static SpringJaxRsHandler registerToRouter(final Router router,
        final Class<? extends Application> applicationClass) {

        return multipleRegisterToRouter(router, applicationClass)[0];
    }

    /**
     * Convenience method to construct and register a single application route to a
     * Vert.x router.
     *
     * @param router
     *            vert.x router
     * @param applicationContext
     *            application context
     * @param applicationClass
     *            application class
     * @return the handler
     */
    public static SpringJaxRsHandler registerToRouter(final Router router,
        final ConfigurableApplicationContext applicationContext,
        final Class<? extends Application> applicationClass) {

        return multipleRegisterToRouter(router, applicationContext, applicationClass)[0];
    }

    private final AnnotationConfigApplicationContext applicationContext;

    private final URI baseUri;

    private final ResteasyClientBuilder clientBuilder;

    private final ResteasyDeployment deployment;

    private final SynchronousDispatcher dispatcher;

    private final ResteasyProviderFactory providerFactory;

    public SpringJaxRsHandler(
        final Class<? extends Application> applicationClass) {

        this(new StaticApplicationContext(), applicationClass);
    }

    public SpringJaxRsHandler(final ConfigurableApplicationContext baseApplicationContext,
        final Class<? extends Application> applicationClass) {

        if (!baseApplicationContext.isActive()) {
            baseApplicationContext.refresh();
        }
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setParent(baseApplicationContext);
        applicationContext.register(SpringConfiguration.class, applicationClass, VertxRequestContextFilter.class);

        final ApplicationPath annotation = applicationClass.getAnnotation(ApplicationPath.class);
        if (annotation != null) {
            baseUri = URI.create(annotation.value() + "").normalize();
        } else {
            baseUri = URI.create("");
        }

        Application application;
        try {
            application = applicationClass.newInstance();
        } catch (InstantiationException
            | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }

        // Use URLConnectionEngine
        clientBuilder = new ResteasyClientBuilder().httpEngine(new URLConnectionEngine());
        final Set<Class<?>> resourceClasses = application.getClasses();
        if (resourceClasses.isEmpty()) {
            final String packageName = applicationClass.getPackage().getName();
            LOG.debug("getClasses() is empty, performing scanning of annotated JAX-RS classes from {}", packageName);
            final Reflections reflections = new Reflections(packageName);
            reflections.getTypesAnnotatedWith(Path.class).forEach(clazz -> {
                LOG.debug("{} => @Path {}", baseUri + clazz.getAnnotation(Path.class).value(), clazz);
                applicationContext.register(clazz);
            });
            reflections.getTypesAnnotatedWith(Provider.class).forEach(clazz -> applicationContext.register(clazz));
        } else {
            LOG.debug("getClasses() = {}, not performing any scan", resourceClasses);
            resourceClasses.forEach(clazz -> applicationContext.register(clazz));
        }
        deployment = new ResteasyDeployment();
        deployment.setApplication(application);
        deployment.start();

        final SpringBeanProcessor springBeanProcessor = new SpringBeanProcessor(deployment);

        applicationContext.addBeanFactoryPostProcessor(springBeanProcessor);
        applicationContext.addApplicationListener(springBeanProcessor);
        applicationContext.refresh();
        baseApplicationContext.getBeansWithAnnotation(Provider.class).forEach(
            (name,
                obj) -> {
                System.out.println("registering {} into provider factory" + name);
                deployment.getProviderFactory().register(obj);
            });

        baseApplicationContext.getBeansWithAnnotation(Path.class).forEach(
            (name,
                obj) -> {
                System.out.println("registering {} into provider factory" + name);
                deployment.getProviderFactory().register(obj);
            });
        deployment.start();
        dispatcher = (SynchronousDispatcher) deployment.getDispatcher();
        providerFactory = deployment.getProviderFactory();
    }

    /**
     * The base URI set in {@link ApplicationPath} annotation.
     *
     * @return the base URI
     */
    public URI baseUri() {

        return baseUri;
    }

    /**
     * The route path to the base URI. Basically base URI + "/*".
     *
     * @return The route path to the base URI.
     */
    public String baseUriRoute() {

        return baseUri().toASCIIString() + "/*";
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} {}", context.request().method(), context.request().absoluteURI());
                }
                try {
                    ThreadLocalResteasyProviderFactory.push(providerFactory);
                    try {
                        ResteasyProviderFactory.pushContext(RoutingContext.class, context);
                        ResteasyProviderFactory.pushContext(Vertx.class, context.vertx());
                        ResteasyProviderFactory.pushContext(Client.class, clientBuilder.build());

                        final Application application = deployment.getApplication();
                        dispatcher.invokePropagateNotFound(new VertxHttpRequest(context,
                            URI.create(application.getClass().getAnnotation(ApplicationPath.class).value()), dispatcher),
                            new VertxHttpResponse(context));
                        future.complete();
                    } finally {
                        ResteasyProviderFactory.clearContextData();
                    }
                } finally {
                    ThreadLocalResteasyProviderFactory.pop();
                }
            },
            res -> {
                if (res.failed()) {
                    final Throwable wae = res.cause();
                    if (wae instanceof NotFoundException) {
                        LOG.debug("uri={} was not found", context.request().absoluteURI());
                        context.response().setStatusCode(404);
                        context.response().setStatusMessage(Status.NOT_FOUND.getReasonPhrase());
                        if (context.request().method() != HttpMethod.HEAD) {
                            context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
                            context.response().end(wae.getLocalizedMessage());
                        }
                    } else {
                        LOG.error(wae.getMessage(), wae);
                        context.response().setStatusCode(500);
                        context.response().setStatusMessage(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                        if (context.request().method() != HttpMethod.HEAD) {
                            context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
                            context.response().end(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                        }
                    }
                } else {
                    context.response().end();
                }
            });
    }

}
