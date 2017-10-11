package net.trajano.ms.engine.jaxrs;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.Json;

/**
 * This {@link ObjectMapper} sets up Jackson for some common defaults. This
 * utilizes Vert.x mapper to support their json objects in addition it will
 * prevent <code>null</code> from being put into a resulting JSON.
 *
 * @author Archimedes Trajano
 */
@Component
@Provider
public class CommonObjectMapper implements
    ContextResolver<ObjectMapper> {

    private static final ObjectMapper MAPPER;
    static {
        MAPPER = Json.mapper.copy();
        MAPPER.setSerializationInclusion(Include.NON_NULL);
    }

    @Override
    public ObjectMapper getContext(final Class<?> clazz) {

        return MAPPER;
    }
}
