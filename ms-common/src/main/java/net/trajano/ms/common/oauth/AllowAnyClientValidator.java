package net.trajano.ms.common.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will allow any client.
 *
 * @author Archimedes Trajano
 */
public class AllowAnyClientValidator implements
    ClientValidator {

    private static final Logger LOG = LoggerFactory.getLogger(AllowAnyClientValidator.class);

    @Override
    public boolean isValid(final String grantType,
        final String clientId,
        final String clientSecret) {

        LOG.warn("AllowAnyClientValidator is being used");
        LOG.debug("grantType={} clientId={} clientSecret={}", grantType, clientId, clientSecret);
        return grantType != null && clientId != null && clientSecret != null;
    }

}
