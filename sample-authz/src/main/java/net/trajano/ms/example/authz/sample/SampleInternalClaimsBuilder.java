package net.trajano.ms.example.authz.sample;

import com.nimbusds.jwt.JWTClaimsSet;
import net.trajano.ms.example.authz.InternalClaimsBuilder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SampleInternalClaimsBuilder implements
    InternalClaimsBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public JWTClaimsSet.Builder buildInternalJWTClaimsSet(final JWTClaimsSet claims) {

        return new JWTClaimsSet.Builder()
            .subject("Internal-Subject" + claims.getSubject())
            .claim("roles", Arrays.asList("user"));
    }

}
