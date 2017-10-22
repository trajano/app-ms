package net.trajano.ms.auth.test;

import javax.ws.rs.InternalServerErrorException;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.stereotype.Component;

import net.trajano.ms.authz.spi.InternalClaimsBuilder;
import net.trajano.ms.core.Qualifiers;

@Component
public class SampleInternalClaimsBuilder implements
    InternalClaimsBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaims buildInternalJWTClaimsSet(final String assertion) {

        try {

            final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .build();

            final JwtClaims newClaims = jwtConsumer.processToClaims(assertion);
            newClaims.setSubject("internal-subject-" + newClaims.getSubject());
            newClaims.setStringListClaim(Qualifiers.ROLES, "users");
            return newClaims;
        } catch (MalformedClaimException
            | InvalidJwtException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
