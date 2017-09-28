package net.trajano.ms.common;

import javax.ws.rs.container.ContainerRequestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks if the URI is flagged as JwtNotRequired and if so will bypass checks
 * for the given resource.
 *
 * @author Archimedes Trajano
 */
public class DefaultAssertionRequiredFunction implements
    JwtAssertionRequiredFunction {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAssertionRequiredFunction.class);

    @Override
    public Boolean apply(final ContainerRequestContext context) {

        final String path = context.getUriInfo().getPath();
        LOG.debug("path={}", path);
        if ("/jwks".equals(path)) {
            return false;
        } else if ("/info".equals(path)) {
            return false;
        } else if ("/".equals(path)) {
            return false;
        } else {
            return true;
        }
    }

}
