package net.trajano.ms.common;

import javax.ws.rs.container.ResourceInfo;

/**
 * Checks if the URI is flagged as JwtNotRequired and if so will bypass checks
 * for the given resource.
 *
 * @author Archimedes Trajano
 */
public class DefaultAssertionRequiredFunction implements
    JwtAssertionRequiredFunction {

    @Override
    public boolean test(final ResourceInfo resourceInfo) {

        return resourceInfo.getResourceMethod().getAnnotation(JwtNotRequired.class) == null &&
            resourceInfo.getResourceClass().getAnnotation(JwtNotRequired.class) == null;
    }

}
