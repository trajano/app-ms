package net.trajano.ms.engine.internal.spring;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Wraps a {@link ContainerRequestContext} as a scope. This will eventually
 * replace RequestScope from Spring web and remove that dependency from the
 * project.
 *
 * @author Archimedes Trajano
 */
public class ContainerRequestScope implements
    Scope {

    private final ContainerRequestContext containerRequest;

    private final Map<String, Runnable> destructionCallbacks = new HashMap<>();

    public ContainerRequestScope(final ContainerRequestContext containerRequest) {

        this.containerRequest = containerRequest;
    }

    @Override
    public Object get(final String name,
        final ObjectFactory<?> objectFactory) {

        Object scopedObject = containerRequest.getProperty(name);
        if (scopedObject == null) {
            scopedObject = objectFactory.getObject();
            containerRequest.setProperty(name, scopedObject);
        }
        return scopedObject;
    }

    @Override
    public String getConversationId() {

        return containerRequest.getRequest().toString();
    }

    @Override
    public void registerDestructionCallback(final String name,
        final Runnable callback) {

        destructionCallbacks.put(name, callback);

    }

    @Override
    public Object remove(final String name) {

        final Object scopedObject = containerRequest.getProperty(name);
        if (scopedObject != null) {
            containerRequest.removeProperty(name);

            destructionCallbacks.remove(name);
            return scopedObject;
        } else {
            return null;
        }
    }

    @Override
    public Object resolveContextualObject(final String key) {

        return "request";
    }

}
