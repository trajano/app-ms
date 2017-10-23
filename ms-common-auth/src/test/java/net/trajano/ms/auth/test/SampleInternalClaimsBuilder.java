package net.trajano.ms.auth.test;

import javax.ws.rs.InternalServerErrorException;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
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
    public JwtClaims buildInternalJWTClaimsSet(final JwtClaims claims) {

        try {

            final JwtClaims newClaims = claims;
            newClaims.setSubject("internal-subject-" + newClaims.getSubject());
            newClaims.setStringListClaim(Qualifiers.ROLES, "users");
            return newClaims;
        } catch (final MalformedClaimException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
