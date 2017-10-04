package net.trajano.ms.sample;

import java.text.ParseException;

import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.common.JwtClaimsProcessor;

@Component
public class ValidatingProcessor implements
    JwtClaimsProcessor {

    private final String claimName = "boo";

    private final String claimValue = "ya";

    @Override
    public Boolean apply(final JWTClaimsSet claims) {

        System.out.println("ABC");
        try {
            return claimValue.equals(claims.getStringClaim(claimName));
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
