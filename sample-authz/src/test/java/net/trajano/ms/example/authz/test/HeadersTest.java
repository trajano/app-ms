package net.trajano.ms.example.authz.test;

import net.trajano.ms.auth.internal.HttpAuthorizationHeaders;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class HeadersTest {

    @Test
    public void testAuthorization() {

        String authorization = "Basic YXNkZjphc2Rm";
        String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);
        assertNotNull(clientCredentials);
    }
}
