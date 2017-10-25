package net.trajano.ms.oidc.internal;

import java.io.Serializable;

import javax.ws.rs.InternalServerErrorException;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;

/**
 * State that is stored on the cache. Mapped by a new token. The reason a new
 * token was used rather than relying on the nonce is to avoid making the call
 * to the IP to validate the nonce.
 *
 * @author Archimedes Trajano
 */
public class ServerState implements
    Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -8675497431930478448L;

    /**
     * Server operation.
     */
    private final String additionalClaimsJson;

    /**
     * Client credentials. This will be passed to the op
     */
    private final String clientCredentials;

    /**
     * Client state.
     */
    private final String clientState;

    /**
     * Nonce.
     */
    private final String nonce;

    /**
     * Constructs ServerState.
     *
     * @param clientState
     *            client side state
     * @param additionalClaims
     *            additional claims
     * @param nonce
     *            nonce
     */
    public ServerState(final String clientState,
        final JwtClaims additionalClaims,
        final String nonce,
        final String clientCredentials) {

        this.clientState = clientState;
        additionalClaimsJson = additionalClaims.toJson();
        this.nonce = nonce;
        this.clientCredentials = clientCredentials;
    }

    public JwtClaims getAdditionalClaims() {

        try {
            return JwtClaims.parse(additionalClaimsJson);
        } catch (final InvalidJwtException e) {
            throw new InternalServerErrorException(e);
        }
    }

    public String getClientCredentials() {

        return clientCredentials;
    }

    public String getClientState() {

        return clientState;
    }

    public String getNonce() {

        return nonce;
    }

}
