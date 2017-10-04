package net.trajano.ms.common.beans;

import javax.ws.rs.container.ResourceInfo;

import net.trajano.ms.common.JwtNotRequired;

/**
 * Checks if the URI is flagged as JwtNotRequired and if so will bypass checks
 * for the given resource.
 *
 * @author Archimedes Trajano
 */
public class DefaultAssertionRequiredFunction implements
    JwtAssertionRequiredPredicate {

    @Override
    public boolean test(final ResourceInfo resourceInfo) {

        return resourceInfo.getResourceMethod().getAnnotation(JwtNotRequired.class) == null &&
            resourceInfo.getResourceClass().getAnnotation(JwtNotRequired.class) == null;
    }

}
