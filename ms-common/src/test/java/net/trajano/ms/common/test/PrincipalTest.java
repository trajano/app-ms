package net.trajano.ms.common.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.common.IdTokenPrincipal;

public class PrincipalTest {

    @Test
    public void testAuthority() {

        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issuer("https://accounts.trajano.net").subject("archie").build();
        final IdTokenPrincipal idTokenPrincipal = new IdTokenPrincipal(claimsSet);
        assertEquals("archie", idTokenPrincipal.getName());
        assertEquals("archie@accounts.trajano.net", idTokenPrincipal.getAuthority());
        assertEquals(claimsSet, idTokenPrincipal.getClaimsSet());
    }
}
