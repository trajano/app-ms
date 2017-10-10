package net.trajano.ms.example.authz.sample;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.example.authz.InternalClaimsBuilder;

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
