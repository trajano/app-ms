package net.trajano.ms.auth.spi;

import java.net.URI;
import java.text.ParseException;

import net.trajano.ms.auth.util.HttpAuthorizationHeaders;

/**
 * This is used to validate the client ID and secret.
 *
 * @author Archimedes Trajano
 */
public interface ClientValidator {

    /**
     * Gets the JWKS URI associated with the client ID.
     *
     * @param clientId
     *            client ID
     * @return
     */
    default URI getJwksUri(final String clientId) {

        return null;
    }

    default boolean isValid(final String grantType,
        final String authorization) throws ParseException {

        final String[] authInfo = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);
        return isValid(grantType, authInfo[0], authInfo[1]);
    }

    boolean isValid(String grantType,
        String clientId,
        String clientSecret);
}
