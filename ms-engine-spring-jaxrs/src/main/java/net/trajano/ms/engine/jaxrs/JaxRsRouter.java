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
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * This will take the JAX-RS annotated classes and create individual routes on a
 * VertX router.
 */
@Component
public class JaxRsRouter {

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsRouter.class);

    @Autowired
    private JaxRsFailureHandler failureHandler;

    /**
     * Gets the {@link HttpMethod} based on the annotation associated with the
     * method. Only GET, POST, PUT, DELETE are supported.
     *
     * @param m
     *            method
     * @return {@link HttpMethod}
     */
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

    /**
     * Register the routes.
     *
     * @param applicationClass
     *            application class to get the root
     * @param router
     *            router to apply the changes to
     * @param pathsProvider
     *            provides a list of Path classes
     * @param jaxRsHandler
     *            route handler
     */
    public void register(final Class<? extends Application> applicationClass,
        final Router router,
        final PathsProvider pathsProvider,
        final Handler<RoutingContext> jaxRsHandler) {

        final String rootPath = Optional.ofNullable(applicationClass.getAnnotation(ApplicationPath.class)).map(ApplicationPath::value).orElse("");

        final SortedSet<JaxRsPath> paths = new TreeSet<>();
        pathsProvider.getPathAnnotatedClasses().forEach(clazz -> {
            final String classPath = Optional.ofNullable(clazz.getAnnotation(Path.class)).map(Path::value).orElse("");
            stream(clazz.getMethods()).filter(m -> m.getAnnotation(GET.class) != null
                || m.getAnnotation(POST.class) != null
                || m.getAnnotation(PUT.class) != null
                || m.getAnnotation(DELETE.class) != null).forEach(m -> {
                    final String path = Optional.ofNullable(m.getAnnotation(Path.class)).map(Path::value).orElse("");
                    final String[] consumes = Optional.ofNullable(m.getAnnotation(Consumes.class)).map(Consumes::value).orElse(new String[0]);
                    final String[] produces = Optional.ofNullable(m.getAnnotation(Produces.class)).map(Produces::value).orElse(new String[0]);

                    paths.add(new JaxRsPath(UriBuilder.fromPath(rootPath).path(classPath).path(path).toTemplate(), consumes, produces, getHttpMethod(m)));

                });
        });
        paths.forEach(p -> {
            p.apply(router, jaxRsHandler, failureHandler);
            LOG.debug("route={}", p);
        });

    }

    public void setFailureHandler(final JaxRsFailureHandler failureHandler) {

        this.failureHandler = failureHandler;
    }
}
