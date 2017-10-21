package net.trajano.ms.sample;

import net.trajano.ms.vertx.beans.JwtClaimsProcessor;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.springframework.stereotype.Component;

import javax.ws.rs.InternalServerErrorException;

@Component
public class ValidatingProcessor implements
    JwtClaimsProcessor {

    private final String claimName = "boo";

    private final String claimValue = "ya";

    @Override
    public Boolean apply(final JwtClaims claims) {

        try {
            return claimValue.equals(claims.getStringClaimValue(claimName));
        } catch (final MalformedClaimException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
