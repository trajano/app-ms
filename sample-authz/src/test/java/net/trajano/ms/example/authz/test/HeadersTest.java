package net.trajano.ms.example.authz.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import net.trajano.ms.auth.util.HttpAuthorizationHeaders;

public class HeadersTest {

    @Test
    public void testAuthorization() {

        final String authorization = "Basic YXNkZjphc2Rm";
        final String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);
        assertNotNull(clientCredentials);
    }
}
