package net.trajano.ms.auth.test;

import net.trajano.ms.auth.spi.InternalClaimsBuilder;
import net.trajano.ms.core.JwtClaimsSetPrincipal;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.stereotype.Component;

import javax.ws.rs.InternalServerErrorException;

@Component
public class SampleInternalClaimsBuilder implements
    InternalClaimsBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaims buildInternalJWTClaimsSet(String assertion) {

        try {

            final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .build();

            JwtClaims newClaims = jwtConsumer.processToClaims(assertion);
            newClaims.setSubject("internal-subject-" + newClaims.getSubject());
            newClaims.setStringListClaim(JwtClaimsSetPrincipal.ROLES, "users");
            return newClaims;
        } catch (MalformedClaimException
            | InvalidJwtException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
