package net.trajano.ms.oidc.internal;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.UriBuilder;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.core.ErrorResponses;
import net.trajano.ms.oidc.spi.IssuerConfig;
import net.trajano.ms.oidc.spi.ServiceConfiguration;

/**
 * Provides a method to build an authentication URI.
 *
 * @author Archimedes Trajano
 */
@Component
public class AuthenticationUriBuilder {

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
            throw ErrorResponses.invalidRequest("Missing issuer_id");
        }
        if (state == null) {
            throw ErrorResponses.invalidRequest("Missing state");
        }

        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            throw ErrorResponses.invalidRequest("Invalid issuer_id");
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
