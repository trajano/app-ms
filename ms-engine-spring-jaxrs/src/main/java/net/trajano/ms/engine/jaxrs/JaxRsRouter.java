package net.trajano.ms.engine.jaxrs;

import static java.util.Arrays.stream;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * This will take the JAX-RS annotated classes and create individual routes on a
 * VertX router.
 */
@Component
public class JaxRsRouter {

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsRouter.class);

    private HttpMethod getHttpMethod(final Method m) {

        if (m.getAnnotation(GET.class) != null) {
            return HttpMethod.GET;
        } else if (m.getAnnotation(POST.class) != null) {
            return HttpMethod.POST;
        } else if (m.getAnnotation(PUT.class) != null) {
            return HttpMethod.PUT;
        } else if (m.getAnnotation(DELETE.class) != null) {
            return HttpMethod.DELETE;
        } else {
            throw new IllegalStateException("Unabel to determine HTTP Method");
        }
    }

    public void register(final Class<? extends Application> applicationClass,
        final Router router,
        final Handler<RoutingContext> jaxRsHandler) {

        final Reflections reflections = new Reflections(applicationClass);
        final String rootPath = Optional.ofNullable(applicationClass.getAnnotation(ApplicationPath.class)).map(ApplicationPath::value).orElse("/");
        final JaxRsFailureHandler failureHandler = new JaxRsFailureHandler();

        final SortedSet<JaxRsPath> paths = new TreeSet<>();
        reflections.getTypesAnnotatedWith(Path.class).forEach(clazz -> {
            final String classPath = Optional.ofNullable(clazz.getAnnotation(Path.class)).map(Path::value).orElse("");
            stream(clazz.getMethods()).filter(m -> m.getAnnotation(GET.class) != null
                || m.getAnnotation(POST.class) != null
                || m.getAnnotation(PUT.class) != null
                || m.getAnnotation(DELETE.class) != null).forEach(m -> {
                    final String path = Optional.ofNullable(m.getAnnotation(Path.class)).map(Path::value).orElse("");
                    final String[] consumes = Optional.ofNullable(m.getAnnotation(Consumes.class)).map(Consumes::value).orElse(new String[0]);
                    final String[] produces = Optional.ofNullable(m.getAnnotation(Produces.class)).map(Produces::value).orElse(new String[0]);

                    paths.add(new JaxRsPath(rootPath + classPath + path, consumes, produces, getHttpMethod(m)));

                });
        });
        paths.stream().filter(p -> !p.isGet()).forEach(p -> stream(p.getConsumes()).forEach(consumes -> stream(p.getProduces()).forEach(produces -> {
            final Route route;
            if (p.isExact()) {
                route = router.route(p.getMethod(), p.getPath());
            } else {
                route = router.routeWithRegex(p.getMethod(), p.getPathRegex());
            }
            route.consumes(consumes).produces(produces).handler(jaxRsHandler).failureHandler(failureHandler);

        })));
        paths.stream().filter(p -> !p.isGet() && p.isNoProduces()).forEach(p -> stream(p.getConsumes()).forEach(consumes -> {
            final Route route;
            if (p.isExact()) {
                route = router.route(p.getMethod(), p.getPath());
            } else {
                route = router.routeWithRegex(p.getMethod(), p.getPathRegex());
            }
            route.consumes(consumes).handler(jaxRsHandler).failureHandler(failureHandler);

        }));
        paths.stream().filter(JaxRsPath::isGet).forEach(p -> stream(p.getProduces()).forEach(produces -> {
            final Route getRoute;
            final Route headRoute;
            if (p.isExact()) {
                getRoute = router.get(p.getPath());
                headRoute = router.head(p.getPath());
            } else {
                getRoute = router.getWithRegex(p.getPathRegex());
                headRoute = router.headWithRegex(p.getPathRegex());
            }
            getRoute.produces(produces).handler(jaxRsHandler).failureHandler(failureHandler);
            headRoute.handler(jaxRsHandler).failureHandler(failureHandler);
            LOG.debug("path={}", p);
        }));
        paths.stream().filter(p -> p.isGet() && p.isNoProduces()).forEach(p -> {
            final Route getRoute;
            final Route headRoute;
            if (p.isExact()) {
                getRoute = router.get(p.getPath());
                headRoute = router.head(p.getPath());
            } else {
                getRoute = router.getWithRegex(p.getPathRegex());
                headRoute = router.headWithRegex(p.getPathRegex());
            }
            getRoute.handler(jaxRsHandler).failureHandler(failureHandler);
            headRoute.handler(jaxRsHandler).failureHandler(failureHandler);
            LOG.debug("path={}", p);
        });

    }
}
