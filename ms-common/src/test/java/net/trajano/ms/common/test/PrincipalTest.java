package net.trajano.ms.common.test;

import net.trajano.ms.core.JwtClaimsSetPrincipal;
import org.jose4j.jwt.JwtClaims;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PrincipalTest {

    @Test
    public void testAuthority() {

        final JwtClaims claims = new JwtClaims();
        claims.setIssuer("https://accounts.trajano.net");
        claims.setSubject("archie");

        final JwtClaimsSetPrincipal idTokenPrincipal = new JwtClaimsSetPrincipal(claims);
        assertEquals("archie", idTokenPrincipal.getName());
        assertEquals("archie@accounts.trajano.net", idTokenPrincipal.getAuthority());
        assertEquals(claims, idTokenPrincipal.getClaimsSet());
    }
}
