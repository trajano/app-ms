package net.trajano.ms.swagger.internal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.auth.SecuritySchemeDefinition;

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

    public ClonableSwagger getSwagger(final String basePath,
        final UriInfo uriInfo) {

        ClonableSwagger swagger = swaggerMap.get(basePath);
        if (swagger == null) {
            final int i = swaggerPaths.get(basePath);

            swagger = new ClonableSwagger();
            final String title = env.getProperty(String.format("swagger[%d].info.title", i));
            final String version = env.getProperty(String.format("swagger[%d].info.version", i));

            swagger.setBasePath('/' + basePath);
            final Info info = new Info();
            info.setTitle(title);
            info.setVersion(version);
            swagger.setInfo(info);
            final Map<String, Path> pathsMap = new TreeMap<>();
            final Map<String, Model> definitionsMap = new TreeMap<>();
            final Map<String, SecuritySchemeDefinition> securityDefinitionsMap = new TreeMap<>();
            processUris(pathsMap, definitionsMap, securityDefinitionsMap, i);
            swagger.setPaths(pathsMap);
            swagger.setDefinitions(definitionsMap);
            swagger.setSecurityDefinitions(securityDefinitionsMap);

            swaggerMap.putIfAbsent(basePath, swagger);
        }
        return swagger.withUriInfo(uriInfo);
    }

    @PostConstruct
    public void init() {

        int i = 0;
        while (env.containsProperty(String.format("swagger[%d].path", i))) {

            final String basePath = env.getProperty(String.format("swagger[%d].path", i));
            if (!basePath.startsWith("/")) {
                throw new IllegalArgumentException("Paths must begin with /");
            }
            swaggerPaths.put(basePath.substring(1), i);
            ++i;
        }

    }

    /**
     * Checks to see if the base path specification exists.
     *
     * @param basePath
     *            base path
     * @return true if it is registered.
     */
    public boolean isPathExists(final String basePath) {

        return swaggerPaths.containsKey(basePath);
    }

    private void processPaths(final Map<String, Path> swagger,
        final Map<String, Model> definitionsMap,
        final Map<String, SecuritySchemeDefinition> securityDefinitionsMap,
        final Swagger remoteSwagger,
        final int i,
        final int j) {

        int k = 0;
        while (env.containsProperty(String.format("swagger[%d].uris[%d].paths[%d].from", i, j, k)) || env.containsProperty(String.format("swagger[%d].uris[%d].paths[%d]", i, j, k))) {
            if (env.containsProperty(String.format("swagger[%d].uris[%d].paths[%d].from", i, j, k))) {
                final String from = env.getRequiredProperty(String.format("swagger[%d].uris[%d].paths[%d].from", i, j, k));
                final String to = env.getRequiredProperty(String.format("swagger[%d].uris[%d].paths[%d].to", i, j, k));

                LOG.debug("getting path={} from paths={} and transform to={}", from, remoteSwagger.getPaths().keySet(), to);
                Optional.ofNullable(remoteSwagger.getPaths())
                    .ifPresent(t -> remoteSwagger.getPaths().keySet().parallelStream()
                        .filter(s -> s.startsWith(from))
                        .forEach(p -> swagger.put(to + p.substring(from.length()), remoteSwagger.getPath(p))));

                updateDefinitions(definitionsMap, securityDefinitionsMap, remoteSwagger);
            } else {
                final String path = env.getRequiredProperty(String.format("swagger[%d].uris[%d].paths[%d]", i, j, k));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getting path={} from paths={}", path, remoteSwagger.getPaths().keySet());
                }
                if (remoteSwagger.getPaths() != null) {
                    remoteSwagger.getPaths().keySet().parallelStream()
                        .filter(s -> s.startsWith(path))
                        .forEach(p -> swagger.put(p, remoteSwagger.getPath(p)));
                }
                updateDefinitions(definitionsMap, securityDefinitionsMap, remoteSwagger);
            }
            ++k;
        }
    }

    private void processUris(final Map<String, Path> swagger,
        final Map<String, Model> definitionsMap,
        final Map<String, SecuritySchemeDefinition> securityDefinitionsMap,
        final int i) {

        int j = 0;
        while (env.containsProperty(String.format("swagger[%d].uris[%d].swagger", i, j))) {
            final URL swaggerUrl = env.getProperty(String.format("swagger[%d].uris[%d].swagger", i, j), URL.class);
            try {

                final Swagger remoteSwagger = io.swagger.util.Json.mapper().readerFor(Swagger.class).readValue(swaggerUrl.openConnection().getInputStream());

                processPaths(swagger, definitionsMap, securityDefinitionsMap, remoteSwagger, i, j);
                ++j;
            } catch (final IOException e) {
                throw new UncheckedIOException("IOException processing " + swaggerUrl, e);
            }

        }

    }

    /**
     * Update the current definition maps.
     *
     * @param currentDefinitionsMap
     *            current definitions map
     * @param currentSecurityDefinitionsMap
     *            current security defintions map
     * @param swagger
     *            remote swagger data
     */
    private void updateDefinitions(final Map<String, Model> currentDefinitionsMap,
        final Map<String, SecuritySchemeDefinition> currentSecurityDefinitionsMap,
        final Swagger swagger) {

        Optional.ofNullable(swagger.getDefinitions())
            .ifPresent(t -> t.entrySet().parallelStream()
                .forEach(e -> currentDefinitionsMap.put(e.getKey(), e.getValue())));

        Optional.ofNullable(swagger.getSecurityDefinitions())
            .ifPresent(t -> t.entrySet().parallelStream()
                .forEach(e -> currentSecurityDefinitionsMap.put(e.getKey(), e.getValue())));
    }

}
