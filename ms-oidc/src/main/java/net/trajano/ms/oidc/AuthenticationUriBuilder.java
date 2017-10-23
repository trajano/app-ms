package net.trajano.ms.oidc;

import java.net.URI;
import java.text.ParseException;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.UriBuilder;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.token.OAuthTokenResponse;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.authz.spi.ClientValidator;
import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.oidc.internal.HazelcastConfiguration;
import net.trajano.ms.oidc.internal.IssuerConfig;
import net.trajano.ms.oidc.internal.ServerState;
import net.trajano.ms.oidc.internal.ServiceConfiguration;

/**
 * Provides a method to build an authentication URI.
 *
 * @author Archimedes Trajano
 */
@Component
public class AuthenticationUriBuilder {

    @Autowired
    private ClientValidator clientValidator;

    @Autowired
    private CacheManager cm;

    @Autowired
    private CryptoOps cryptoOps;

    private Cache nonceCache;

    @Value("${realmName:client_credentials}")
    private String realmName;

    @Autowired
    private ServiceConfiguration serviceConfiguration;

    /**
     * This builds the authentication URI.
     *
     * @param state
     *            client state
     * @param issuerId
     *            issuer ID
     * @param authorization
     *            Authorization header (for the client ID and client secret)
     * @param additionalClaims
     *            additional claims.
     * @return URI to authenticate with the IP.
     */
    public URI build(final String state,
        final String issuerId,
        final String authorization,
        final JwtClaims additionalClaims) {

        if (issuerId == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Missing issuer_id");
        }
        if (state == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Missing state");
        }

        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Invalid issuer_id");
        }

        // TODO validate whether the client credentials are valid.
        try {
            final String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);
            if (!clientValidator.isValid(GrantTypes.OPENID, clientCredentials[0], clientCredentials[1])) {
                throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Unauthorized client", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
            }
        } catch (final ParseException e) {
            throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Unable to parse client credentials", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
        }

        final URI redirectUri = UriBuilder.fromUri(serviceConfiguration.getRedirectUri()).path(issuerId).build();
        final String nonce = cryptoOps.newToken();

        final ServerState serverState = new ServerState(state, additionalClaims, nonce, authorization);
        final String cacheKey = cryptoOps.newToken();

        nonceCache.putIfAbsent(cacheKey, serverState);

        return issuerConfig.buildAuthenticationRequestUri(redirectUri, cacheKey, nonce);
    }

    @PostConstruct
    public void init() {

        nonceCache = cm.getCache(HazelcastConfiguration.SERVER_STATE);
    }

}
