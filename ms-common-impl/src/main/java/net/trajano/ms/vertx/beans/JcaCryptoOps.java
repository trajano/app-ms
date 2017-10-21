package net.trajano.ms.vertx.beans;

import net.trajano.ms.core.CryptoOps;
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

import javax.ws.rs.InternalServerErrorException;

@Component
public class JcaCryptoOps implements
    CryptoOps {

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private JwksProvider jwksProvider;

    @Override
    public String newToken() {

        return tokenGenerator.newToken();

    }

    @Override
    public String sign(JwtClaims claims) {

        try {
            final RsaJsonWebKey aSigningKey = jwksProvider.getASigningKey();
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKeyIdHeaderValue(aSigningKey.getKeyId());
            jws.setKey(aSigningKey.getPrivateKey());
            jws.setAlgorithmHeaderValue(aSigningKey.getAlgorithm());
            jws.sign();
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public JwtClaims toClaimsSet(String jwt,
        JsonWebKeySet jwks) {

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setVerificationKeyResolver(new JwksVerificationKeyResolver(jwks.getJsonWebKeys()))
            .build();

        try {
            return jwtConsumer.processToClaims(jwt);
        } catch (InvalidJwtException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public JwtClaims toClaimsSet(String jwt,
        HttpsJwks httpsJwks) {

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setVerificationKeyResolver(new HttpsJwksVerificationKeyResolver(httpsJwks))
            .build();

        try {
            return jwtConsumer.processToClaims(jwt);
        } catch (InvalidJwtException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
