package net.trajano.ms.example.authz;

import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;

import javax.annotation.PostConstruct;

import org.jboss.resteasy.spi.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.common.beans.JwksProvider;
import net.trajano.ms.common.beans.TokenGenerator;
import net.trajano.ms.common.oauth.IdTokenResponse;
import net.trajano.ms.common.oauth.OAuthException;
import net.trajano.ms.common.oauth.OAuthTokenResponse;

@Configuration
@Component
@CacheConfig(cacheNames = {
    TokenCache.ACCESS_TOKEN_TO_CLAIMS,
    TokenCache.REFRESH_TOKEN_TO_ACCESS_TOKEN,
    TokenCache.REFRESH_TOKEN_TO_CLAIMS
})
public class TokenCache {

    static final String ACCESS_TOKEN_TO_CLAIMS = "accessTokenToClaims";

    static final String REFRESH_TOKEN_TO_ACCESS_TOKEN = "refreshTokenToAccessToken";

    static final String REFRESH_TOKEN_TO_CLAIMS = "refreshTokenToClaims";

    @Value("${token.accessTokenExpiration:300}")
    private int accessTokenExpirationInSeconds;

    private Cache accessTokenToClaims;

    @Autowired
    private CacheManager cm;

    @Autowired
    private JwksProvider jwksProvider;

    private Cache refreshTokenToAccessToken;

    private Cache refreshTokenToClaims;

    @Autowired
    private TokenGenerator tokenGenerator;

    public IdTokenResponse get(final String accessToken) {

        final JWTClaimsSet claims = accessTokenToClaims.get(accessToken, JWTClaimsSet.class);
        final JWSObject jws = new JWSObject(new JWSHeader(JWSAlgorithm.RS512), new Payload(claims.toString()));
        try {
            jws.sign(jwksProvider.getASigner());
            final String jwt = jws.serialize();

            final IdTokenResponse oauthTokenResponse = new IdTokenResponse();
            oauthTokenResponse.setAccessToken(accessToken);
            oauthTokenResponse.setTokenType("JWT-Assertion");
            if (claims.getExpirationTime() != null) {
                oauthTokenResponse.setExpiresIn((int) Duration.between(Instant.now(), claims.getExpirationTime().toInstant()).getSeconds());
            }
            oauthTokenResponse.setIdToken(jwt);
            return oauthTokenResponse;
        } catch (InvalidKeySpecException
            | JOSEException
            | ParseException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @PostConstruct
    public void init() {

        accessTokenToClaims = cm.getCache(ACCESS_TOKEN_TO_CLAIMS);
        refreshTokenToAccessToken = cm.getCache(REFRESH_TOKEN_TO_ACCESS_TOKEN);
        refreshTokenToClaims = cm.getCache(REFRESH_TOKEN_TO_CLAIMS);
    }

    public OAuthTokenResponse refresh(final String oldRefreshToken) {

        final JWTClaimsSet claims = refreshTokenToClaims.get(oldRefreshToken, JWTClaimsSet.class);
        if (claims == null) {
            throw new OAuthException("invalid_request", "Refresh token is not valid");
        }
        refreshTokenToClaims.evict(oldRefreshToken);
        final String oldAccessToken = refreshTokenToAccessToken.get(oldRefreshToken, String.class);
        if (oldAccessToken != null) {
            accessTokenToClaims.evict(oldAccessToken);
            refreshTokenToAccessToken.evict(oldRefreshToken);
        }

        final String accessToken = tokenGenerator.newToken();
        final String refreshToken = tokenGenerator.newToken();

        final OAuthTokenResponse oauthTokenResponse = new OAuthTokenResponse();
        oauthTokenResponse.setAccessToken(accessToken);
        oauthTokenResponse.setTokenType("Bearer");
        oauthTokenResponse.setExpiresIn(accessTokenExpirationInSeconds);
        oauthTokenResponse.setRefreshToken(refreshToken);

        accessTokenToClaims.putIfAbsent(accessToken, claims);
        refreshTokenToClaims.putIfAbsent(refreshToken, claims);
        refreshTokenToAccessToken.putIfAbsent(refreshToken, accessToken);

        return oauthTokenResponse;
    }

    /**
     * Stores the internal claims set into the cache and returns an OAuth token.
     *
     * @param claims
     * @return
     */
    public OAuthTokenResponse store(final JWTClaimsSet claims) {

        final String accessToken = tokenGenerator.newToken();
        final String refreshToken = tokenGenerator.newToken();

        final OAuthTokenResponse oauthTokenResponse = new OAuthTokenResponse();
        oauthTokenResponse.setAccessToken(accessToken);
        oauthTokenResponse.setTokenType("Bearer");
        oauthTokenResponse.setExpiresIn(accessTokenExpirationInSeconds);
        oauthTokenResponse.setRefreshToken(refreshToken);

        accessTokenToClaims.putIfAbsent(accessToken, claims);
        refreshTokenToClaims.putIfAbsent(refreshToken, claims);
        refreshTokenToAccessToken.putIfAbsent(refreshToken, accessToken);

        return oauthTokenResponse;
    }

}
