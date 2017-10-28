package net.trajano.ms.engine.internal.spring;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
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

    private static final ThreadLocal<ContainerRequestContext> containerRequestHolder = new ThreadLocal<>();

    private static final String DESTRUCTION_CALLBACK_PROPERTY = ContainerRequestScope.class.getName() + ".DESTRUCTION_CALLBACKS";

    @SuppressWarnings("unchecked")
    private static Map<String, Runnable> getDestructionCallbacks(@NotNull final ContainerRequestContext containerRequest) {

        return (Map<String, Runnable>) containerRequest.getProperty(DESTRUCTION_CALLBACK_PROPERTY);
    }

    /**
     * This will reset the request context and execute the destruction callbacks.
     */
    public static void resetRequestContext() {

        final ContainerRequestContext containerRequest = containerRequestHolder.get();
        if (containerRequest == null) {
            return;
        }
        final Map<String, Runnable> destructionCallbacks = getDestructionCallbacks(containerRequest);
        containerRequest.getPropertyNames().parallelStream().forEach(name -> {
            final Runnable callback = destructionCallbacks.get(name);
            if (callback != null) {
                callback.run();
            }
        });
        containerRequestHolder.remove();
    }

    public static void setRequestContext(final ContainerRequestContext containerRequest) {

        containerRequestHolder.set(containerRequest);
        containerRequest.setProperty(DESTRUCTION_CALLBACK_PROPERTY, new HashMap<String, Runnable>());
    }

    @Override
    public Object get(final String name,
        final ObjectFactory<?> objectFactory) {

        final ContainerRequestContext containerRequest = containerRequestHolder.get();
        Object scopedObject = containerRequest.getProperty(name);
        if (scopedObject == null) {
            scopedObject = objectFactory.getObject();
            containerRequest.setProperty(name, scopedObject);
        }
        return scopedObject;
    }

    @Override
    public String getConversationId() {

        final ContainerRequestContext containerRequest = containerRequestHolder.get();
        return containerRequest.getRequest().toString();
    }

    @Override
    public void registerDestructionCallback(final String name,
        final Runnable callback) {

        final ContainerRequestContext containerRequest = containerRequestHolder.get();
        getDestructionCallbacks(containerRequest).put(name, callback);

    }

    @Override
    public Object remove(final String name) {

        final ContainerRequestContext containerRequest = containerRequestHolder.get();
        final Object scopedObject = containerRequest.getProperty(name);
        if (scopedObject != null) {
            containerRequest.removeProperty(name);

            getDestructionCallbacks(containerRequest).remove(name);
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
