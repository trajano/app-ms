package net.trajano.ms.gateway.providers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.ClonableSwagger;

@Component
public class SwaggerCollator {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerCollator.class);

    @Autowired
    private ConfigurableEnvironment env;

    private final Map<String, ClonableSwagger> swaggerMap = new ConcurrentHashMap<>();

    private final Map<String, Integer> swaggerPaths = new HashMap<>();

    public Set<String> getPaths() {

        return swaggerPaths.keySet();
    }

    public ClonableSwagger getSwagger(final String basePath) {

        ClonableSwagger swagger = swaggerMap.get(basePath);
        if (swagger == null) {
            final int i = swaggerPaths.get(basePath);

            swagger = new ClonableSwagger();
            final String title = env.getProperty(String.format("swagger[%d].info.title", i));
            final String version = env.getProperty(String.format("swagger[%d].info.version", i));

            swagger.setBasePath(basePath);
            final Info info = new Info();
            info.setTitle(title);
            info.setVersion(version);
            swagger.setInfo(info);
            swagger.setPaths(new TreeMap<>());
            processUris(swagger, i);

            swaggerMap.putIfAbsent(basePath, swagger);
        }
        return swagger;
    }

    public Handler<RoutingContext> handler() {

        return context -> {
            final ClonableSwagger swagger = getSwagger(context.currentRoute().getPath());
            context.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(io.swagger.util.Json.pretty(swagger.withRoutingContext(context)));
        };

    }

    @PostConstruct
    public void init() {

        int i = 0;
        while (env.containsProperty(String.format("swagger[%d].path", i))) {

            final String basePath = env.getProperty(String.format("swagger[%d].path", i));
            swaggerPaths.put(basePath, i);
            ++i;
        }

    }

    private void processPaths(final Swagger swagger,
        final Swagger remoteSwagger,
        final int i,
        final int j) {

        int k = 0;
        while (env.containsProperty(String.format("swagger[%d].uris[%d].paths[%d].from", i, j, k)) || env.containsProperty(String.format("swagger[%d].uris[%d].paths[%d]", i, j, k))) {
            if (env.containsProperty(String.format("swagger[%d].uris[%d].paths[%d].from", i, j, k))) {
                final String from = env.getRequiredProperty(String.format("swagger[%d].uris[%d].paths[%d].from", i, j, k));
                final String to = env.getRequiredProperty(String.format("swagger[%d].uris[%d].paths[%d].to", i, j, k));

                LOG.debug("getting path={} from paths={} and transform to={}", from, remoteSwagger.getPaths().keySet(), to);
                remoteSwagger.getPaths().keySet().parallelStream()
                    .filter(s -> s.startsWith(from))
                    .forEach(p -> swagger.getPaths().put(to + p.substring(from.length()), remoteSwagger.getPath(p)));

            } else {
                final String path = env.getRequiredProperty(String.format("swagger[%d].uris[%d].paths[%d]", i, j, k));
                LOG.debug("getting path={} from paths={}", path, remoteSwagger.getPaths().keySet());
                remoteSwagger.getPaths().keySet().parallelStream()
                    .filter(s -> s.startsWith(path))
                    .forEach(p -> swagger.getPaths().put(p, remoteSwagger.getPath(p)));
            }
            ++k;
        }
    }

    private void processUris(final Swagger swagger,
        final int i) {

        int j = 0;
        while (env.containsProperty(String.format("swagger[%d].uris[%d].swagger", i, j))) {
            try {
                final URL swaggerUrl = env.getProperty(String.format("swagger[%d].uris[%d].swagger", i, j), URL.class);

                final Swagger remoteSwagger = io.swagger.util.Json.mapper().readerFor(Swagger.class).readValue(swaggerUrl.openConnection().getInputStream());

                processPaths(swagger, remoteSwagger, i, j);
                ++j;
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }

        }

    }

}
