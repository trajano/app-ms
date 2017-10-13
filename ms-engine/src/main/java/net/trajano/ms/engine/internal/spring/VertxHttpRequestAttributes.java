package net.trajano.ms.engine.internal.spring;

import javax.ws.rs.container.ContainerRequestContext;

import org.springframework.web.context.request.AbstractRequestAttributes;

public class VertxHttpRequestAttributes extends AbstractRequestAttributes {

    private final ContainerRequestContext requestContext;

    public VertxHttpRequestAttributes(final ContainerRequestContext requestContext) {

        this.requestContext = requestContext;
    }

    @Override
    public Object getAttribute(final String name,
        final int scope) {

        return requestContext.getProperty(name);
    }

    @Override
    public String[] getAttributeNames(final int scope) {

        return requestContext.getPropertyNames().toArray(new String[0]);
    }

    @Override
    public String getSessionId() {

        return null;
    }

    @Override
    public Object getSessionMutex() {

        return null;
    }

    @Override
    public void registerDestructionCallback(final String name,
        final Runnable callback,
        final int scope) {

        registerRequestDestructionCallback(name, callback);
    }

    @Override
    public void removeAttribute(final String name,
        final int scope) {

        requestContext.removeProperty(name);

    }

    @Override
    public Object resolveReference(final String key) {

        if (REFERENCE_REQUEST.equals(key)) {
            return requestContext;
        }
        return null;
    }

    @Override
    public void setAttribute(final String name,
        final Object value,
        final int scope) {

        requestContext.setProperty(name, value);
    }

    @Override
    protected void updateAccessedSessionAttributes() {

    }

}
