package net.trajano.ms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks if the URI is /jwks, /build-info or /swagger and allows them.
 *
 * @author Archimedes Trajano
 */
public class DefaultAssertionRequiredFunction implements
    JwtAssertionRequiredFunction {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAssertionRequiredFunction.class);

    @Override
    public Boolean apply(final String uri) {

        LOG.debug("uri={}", uri);
        if ("/jwks".equals(uri)) {
            return false;
        } else if ("/build-info".equals(uri)) {
            return false;
        } else if ("/swagger".equals(uri)) {
            return false;
        } else {
            return true;
        }
    }

}
