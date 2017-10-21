package net.trajano.ms.common.test;

import net.trajano.ms.core.JwtClaimsSetPrincipal;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PrincipalTest {

    @Test
    public void testAuthority() {

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(JwtClaimsSetPrincipal.ISSUER, "https://accounts.trajano.net");
        claimsMap.put(JwtClaimsSetPrincipal.SUBJECT, "archie");

        final JwtClaimsSetPrincipal idTokenPrincipal = new JwtClaimsSetPrincipal(claimsMap);
        assertEquals("archie", idTokenPrincipal.getName());
        assertEquals("archie@accounts.trajano.net", idTokenPrincipal.getAuthority());
        assertEquals(claimsMap, idTokenPrincipal.getClaimsSet());
    }
}
