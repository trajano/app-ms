package net.trajano.ms.common.oauth;

import java.net.URI;

/**
 * This is used to validate the client ID and secret.
 *
 * @author Archimedes Trajano
 */
public interface ClientValidator {

    boolean isValid(String grantType,
        String clientId,
        String clientSecret);

    /**
     * Gets the JWKS URI associated with the client ID.
     * 
     * @param clientId
     *            client ID
     * @return
     */
    default URI getJwksUri(String clientId) {

        return null;
    }
}
