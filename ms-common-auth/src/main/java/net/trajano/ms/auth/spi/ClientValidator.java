package net.trajano.ms.auth.spi;

import java.net.URI;
import java.text.ParseException;

import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.core.ErrorResponses;

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
     * @return JWKS URI
     */
    default URI getJwksUri(final String clientId) {

        return null;
    }

    default boolean isValid(final String grantType,
        final String authorization) {

        final String[] authInfo = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);
        return isValid(grantType, authInfo[0], authInfo[1]);
    }

    /**
     * Obtains the client redirect URI from the Authorization header.
     * 
     * @param authorization
     *            authorization header value
     * @return redirect URI
     */
    default URI getRedirectUriFromAuthorization(final String authorization) {

        final String[] authInfo = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);
        if (!isValid(GrantTypes.OPENID, authInfo[0], authInfo[1])) {
            throw ErrorResponses.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Unauthorized client", "Basic");
        }
        return getRedirectUri(authInfo[0]);
    }

    URI getRedirectUri(String clientId);

    boolean isValid(String grantType,
        String clientId,
        String clientSecret);
}
