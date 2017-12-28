package net.trajano.ms.engine;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.ResourceInvoker;
import org.jboss.resteasy.core.ThreadLocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.ClassUtils;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.resteasy.VertxClientEngine;
import net.trajano.ms.engine.internal.resteasy.VertxHttpRequest;
import net.trajano.ms.engine.internal.resteasy.VertxHttpResponse;
import net.trajano.ms.engine.internal.spring.CdiScopeMetadataResolver;
import net.trajano.ms.engine.internal.spring.SpringConfiguration;
import net.trajano.ms.engine.internal.spring.VertxRequestContextFilter;
import net.trajano.ms.engine.jaxrs.CommonObjectMapperProvider;
import net.trajano.ms.engine.jaxrs.PathsProvider;
import net.trajano.ms.engine.jaxrs.WebApplicationExceptionMapper;

public class SpringJaxRsHandler implements
    Handler<RoutingContext>,
    AutoCloseable,
    PathsProvider {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SpringJaxRsHandler.class);

    /**
     * Spring application context.
     */
    private final AnnotationConfigApplicationContext applicationContext;

    private final URI baseUri;

    private final ResteasyDeployment deployment;

    private final Dispatcher dispatcher;

    private HttpClientOptions httpClientOptions;

    /**
     * Path annotated classes.
     */
    private final Set<Class<?>> pathAnnotatedClasses;

    private final ResteasyProviderFactory providerFactory;

    public SpringJaxRsHandler(
        final Class<? extends Application> applicationClass) {

        this(null, applicationClass);
    }

    public SpringJaxRsHandler(final ConfigurableApplicationContext baseApplicationContext,
        final Class<? extends Application> applicationClass) {

        if (baseApplicationContext == null) {
            LOG.debug("baseApplicationContext is null, a new application context will be created.");
            applicationContext = new AnnotationConfigApplicationContext();
        } else if (baseApplicationContext.isActive()) {
            LOG.debug("baseApplicationContext={} is active, will use as parent.", baseApplicationContext);
            applicationContext = new AnnotationConfigApplicationContext();
            applicationContext.setParent(baseApplicationContext);
        } else if (!(baseApplicationContext instanceof AnnotationConfigApplicationContext)) {
            LOG.debug("baseApplicationContext={} is not active, but is not an instance of AnnotationConfigApplicationContext, will activate and use as parent.", baseApplicationContext);
            baseApplicationContext.refresh();
            applicationContext = new AnnotationConfigApplicationContext();
            applicationContext.setParent(baseApplicationContext);
        } else {
            LOG.debug("baseApplicationContext={} is not active, will reuse.", baseApplicationContext);
            applicationContext = (AnnotationConfigApplicationContext) baseApplicationContext;
        }
        applicationContext.setScopeMetadataResolver(new CdiScopeMetadataResolver());
        applicationContext.register(SpringConfiguration.class, applicationClass, VertxRequestContextFilter.class, CommonObjectMapperProvider.class, WebApplicationExceptionMapper.class);

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

        pathAnnotatedClasses = new HashSet<>();
        final Set<Class<?>> resourceClasses = application.getClasses();
        if (resourceClasses.isEmpty()) {
            final String packageName = applicationClass.getPackage().getName();
            LOG.debug("getClasses() is empty, performing scanning of annotated JAX-RS classes from {}", packageName);
            final Reflections reflections = new Reflections(packageName);
            reflections.getTypesAnnotatedWith(Path.class).forEach(clazz -> {
                LOG.debug("{} => @Path {}", baseUri + clazz.getAnnotation(Path.class).value(), clazz);
                applicationContext.register(clazz);
            });
            reflections.getTypesAnnotatedWith(Provider.class).forEach(applicationContext::register);
            reflections.getTypesAnnotatedWith(RequestScoped.class).forEach(applicationContext::register);
            reflections.getTypesAnnotatedWith(ApplicationScoped.class).forEach(applicationContext::register);
        } else {
            LOG.debug("getClasses() = {}, not performing any scan", resourceClasses);
            resourceClasses.forEach(applicationContext::register);
        }

        deployment = new ResteasyDeployment();
        deployment.setApplication(application);
        deployment.start();

        final SpringBeanProcessor springBeanProcessor = new SpringBeanProcessor(deployment);

        applicationContext.addBeanFactoryPostProcessor(springBeanProcessor);
        applicationContext.addApplicationListener(springBeanProcessor);
        applicationContext.refresh();
        if (baseApplicationContext != null) {
            baseApplicationContext.getBeansWithAnnotation(Provider.class).forEach(
                (name,
                    obj) -> {
                    if (!deployment.getProviderFactory().isRegistered(obj)) {
                        LOG.debug("registering {} into provider factory", name);
                        deployment.getProviderFactory().register(obj);
                    }
                });
        }
        applicationContext.getBeansWithAnnotation(Path.class).forEach((name,
            obj) -> pathAnnotatedClasses.add(ClassUtils.getUserClass(obj)));

        httpClientOptions = applicationContext.getBean(HttpClientOptions.class);
        if (httpClientOptions == null) {
            httpClientOptions = new HttpClientOptions();
        }
        deployment.start();
        dispatcher = deployment.getDispatcher();
        providerFactory = deployment.getProviderFactory();
    }

    @Override
    public void close() {

        deployment.stop();
        applicationContext.close();

    }

    @Override
    public Iterable<Class<?>> getPathAnnotatedClasses() {

        return pathAnnotatedClasses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final RoutingContext context) {

        final Client client = jaxRsClient(context.vertx());
        final HttpServerRequest serverRequest = context.request();
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(serverRequest.absoluteURI(), serverRequest.query(), baseUri.toASCIIString());

        final VertxHttpRequest request = new VertxHttpRequest(context, uriInfo, providerFactory);
        context.request().setExpectMultipart(isMultipartExpected(request));
        try (final VertxHttpResponse response = new VertxHttpResponse(context)) {
            context.vertx().executeBlocking(
                future -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{} {}", context.request().method(), serverRequest.absoluteURI());
                    }
                    ThreadLocalResteasyProviderFactory.push(providerFactory);
                    ResteasyProviderFactory.pushContext(RoutingContext.class, context);
                    ResteasyProviderFactory.pushContext(Vertx.class, context.vertx());
                    ResteasyProviderFactory.pushContext(Client.class, client);
                    ResteasyProviderFactory.pushContext(HttpHeaders.class, request.getHttpHeaders());

                    try {
                        dispatcher.invoke(request, response);
                        context.response().end();
                        future.complete();
                    } finally {
                        ResteasyProviderFactory.clearContextData();
                        ThreadLocalResteasyProviderFactory.pop();
                    }
                }, false,
                res -> {
                    if (res.failed()) {
                        context.fail(res.cause());
                    }
                    if (!context.response().ended()) {
                        context.response().end();
                    }
                });
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Checks if multipart is expected for the resource. This is determined by the
     * presence of {@value MediaType#MULTIPART_FORM_DATA} in the {@link Consumes}
     * annotation.
     *
     * @param request
     * @return
     */
    private boolean isMultipartExpected(final HttpRequest request) {

        final ResourceInvoker invoker = dispatcher.getRegistry().getResourceInvoker(request);
        final Consumes consumes = invoker.getMethod().getAnnotation(Consumes.class);
        return consumes != null && Arrays.asList(consumes.value()).contains(MediaType.MULTIPART_FORM_DATA);

    }

    /**
     * Get the JAX-RS Client that is present in the vertx context or create a new
     * one.
     *
     * @param vertx
     *            vertx
     * @return JAX-RS client.
     */
    private Client jaxRsClient(final Vertx vertx) {

        Client jaxRsClient = vertx.getOrCreateContext().get(Client.class.getName());
        if (jaxRsClient == null) {
            final HttpClient httpClient = vertx.createHttpClient(httpClientOptions);
            jaxRsClient = new ResteasyClientBuilder().providerFactory(providerFactory).httpEngine(new VertxClientEngine(httpClient)).build();
            //            jaxRsClient = new ResteasyClientBuilder().providerFactory(providerFactory).httpEngine(new ApacheHttpClient43Engine()).build();
            vertx.getOrCreateContext().put(Client.class.getName(), jaxRsClient);
        }
        return jaxRsClient;
    }

}
