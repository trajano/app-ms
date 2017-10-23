package net.trajano.ms.example.authz.test;

import static org.junit.Assert.assertNotNull;

import java.text.ParseException;

import org.junit.Test;

import net.trajano.ms.auth.util.HttpAuthorizationHeaders;

public class HeadersTest {

    @Test
    public void testAuthorization() throws Exception {

        final String authorization = "Basic YXNkZjphc2Rm";
        final String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);
        assertNotNull(clientCredentials);
    }

    @Test(expected = ParseException.class)
    public void testBadAuthorization() throws Exception {

        final String authorization = "aBasic YXNkZjphc2Rm";
        HttpAuthorizationHeaders.parseBasicAuthorization(authorization);

    }
}
