package net.trajano.ms.auth.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;

import net.trajano.ms.auth.util.AuthorizationType;
import org.junit.Test;

import net.trajano.ms.auth.util.HttpAuthorizationHeaders;

import javax.ws.rs.BadRequestException;

public class HeadersTest {

    @Test
    public void testAuthorization() throws Exception {

        final String authorization = "Basic YXNkZjphc2Rm";
        assertEquals(AuthorizationType.BASIC, HttpAuthorizationHeaders.getAuthorizationType(authorization));
        final String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);
        assertNotNull(clientCredentials);
        assertEquals("asdf", clientCredentials[0]);
        assertEquals("asdf", clientCredentials[1]);
    }

    @Test
    public void testBearerAuthorization() throws Exception {

        final String authorization = "Bearer YXNkZjphc2Rm";
        assertEquals(AuthorizationType.BEARER, HttpAuthorizationHeaders.getAuthorizationType(authorization));
        final String clientCredentials = HttpAuthorizationHeaders.parseBeaerAuthorization(authorization);
        assertEquals("YXNkZjphc2Rm", clientCredentials);
    }

    @Test(expected = BadRequestException.class)
    public void testBadAuthorization() throws Exception {

        final String authorization = "aBasic YXNkZjphc2Rm";
        HttpAuthorizationHeaders.parseBasicAuthorization(authorization);

    }

    @Test(expected = BadRequestException.class)
    public void testBadAuthorization2() throws Exception {

        final String authorization = "aBasic YXNkZjphc2Rm";
        HttpAuthorizationHeaders.getAuthorizationType(authorization);

    }
}
