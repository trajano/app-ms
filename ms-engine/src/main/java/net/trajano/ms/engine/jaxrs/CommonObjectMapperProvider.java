package net.trajano.ms.engine.jaxrs;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides the customized object mapper to the JAX-RS environment.
 *
 * @author Archimedes Trajano
 */
@Component
@Provider
@Priority(Priorities.ENTITY_CODER)
public class CommonObjectMapperProvider implements
    ContextResolver<ObjectMapper> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ObjectMapper getContext(final Class<?> clazz) {

        return objectMapper;
    }
}
