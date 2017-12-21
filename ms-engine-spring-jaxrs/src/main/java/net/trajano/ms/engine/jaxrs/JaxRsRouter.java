package net.trajano.ms.engine.jaxrs;

import static java.util.Arrays.stream;

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

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * This will take the JAX-RS annotated classes and create individual routes on a
 * VertX router.
 */
public class JaxRsRouter {

    public void register(final Class<? extends Application> applicationClass,
        final Router router,
        final Handler<RoutingContext> jaxRsHandler) {

        final Reflections reflections = new Reflections(applicationClass);
        final String rootPath = Optional.ofNullable(applicationClass.getAnnotation(ApplicationPath.class)).map(ApplicationPath::value).orElse("/");

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

                    final boolean get = m.getAnnotation(GET.class) != null;
                    final boolean post = m.getAnnotation(POST.class) != null;
                    final boolean put = m.getAnnotation(PUT.class) != null;
                    final boolean delete = m.getAnnotation(DELETE.class) != null;
                    paths.add(new JaxRsPath(rootPath + classPath + path, consumes, produces, get, post, put, delete));
                });
        });
        paths.stream().filter(p -> !p.isGet()).forEach(p -> stream(p.getConsumes()).forEach(consumes -> stream(p.getProduces()).forEach(produces -> {
            final Route route;
            if (p.isPost()) {
                if (p.isExact()) {
                    route = router.post(p.getPath());
                } else {
                    route = router.postWithRegex(p.getPathRegex());
                }
            } else if (p.isPut()) {
                if (p.isExact()) {
                    route = router.put(p.getPath());
                } else {
                    route = router.putWithRegex(p.getPathRegex());
                }
            } else if (p.isDelete()) {
                if (p.isExact()) {
                    route = router.delete(p.getPath());
                } else {
                    route = router.deleteWithRegex(p.getPathRegex());
                }
            } else {
                throw new IllegalStateException("JaxRsPath=" + p + " does not have a HTTP Method active");
            }
            route.consumes(consumes).produces(produces).handler(jaxRsHandler);

        })));
        paths.stream().filter(JaxRsPath::isGet).forEach(p -> stream(p.getProduces()).forEach(produces -> {
            final Route getRoute;
            final Route headRoute;
            if (p.isGet()) {
                if (p.isExact()) {
                    getRoute = router.get(p.getPath());
                    headRoute = router.head(p.getPath());
                } else {
                    getRoute = router.getWithRegex(p.getPathRegex());
                    headRoute = router.head(p.getPathRegex());
                }
            } else {
                throw new IllegalStateException("JaxRsPath=" + p + " does not have a HTTP Method active");
            }
            getRoute.produces(produces).handler(jaxRsHandler);
            headRoute.handler(jaxRsHandler);

        }));

    }
}
