package net.trajano.ms.common.oauth;

/**
 * This is used to validate the client ID and secret.
 *
 * @author Archimedes Trajano
 */
public interface ClientValidator {

    boolean isValid(String grantType,
        String clientId,
        String clientSecret);
}
