package net.trajano.ms.vertx.beans;

import javax.ws.rs.InternalServerErrorException;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.trajano.ms.core.CryptoOps;

@Component
public class JcaCryptoOps implements
    CryptoOps {

    @Autowired
    private CachedDataProvider cachedDataProvider;

    @Autowired
    private TokenGenerator tokenGenerator;

    /**
     * {@inheritDoc}
     */
    @Override
    public String newToken() {

        return tokenGenerator.newToken();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sign(final JwtClaims claims) {

        try {
            final RsaJsonWebKey aSigningKey = cachedDataProvider.getASigningKey();
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKeyIdHeaderValue(aSigningKey.getKeyId());
            jws.setKey(aSigningKey.getPrivateKey());
            jws.setAlgorithmHeaderValue(aSigningKey.getAlgorithm());
            jws.sign();
            return jws.getCompactSerialization();
        } catch (final JoseException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaims toClaimsSet(final String jwt,
        final JsonWebKeySet jwks) {

        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setVerificationKeyResolver(new JwksVerificationKeyResolver(jwks.getJsonWebKeys()))
            .build();

        try {
            return jwtConsumer.processToClaims(jwt);
        } catch (final InvalidJwtException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaims toClaimsSet(final String jwt,
        final String audience,
        final HttpsJwks httpsJwks) {

        final JwtConsumerBuilder builder = new JwtConsumerBuilder()
            .setVerificationKeyResolver(new HttpsJwksVerificationKeyResolver(httpsJwks));
        if (audience == null) {
            builder.setSkipDefaultAudienceValidation();
        } else {
            builder.setExpectedAudience(audience);
        }

        final JwtConsumer jwtConsumer = builder
            .build();

        try {
            return jwtConsumer.processToClaims(jwt);
        } catch (final InvalidJwtException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
