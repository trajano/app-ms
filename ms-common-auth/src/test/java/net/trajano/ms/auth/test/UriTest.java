package net.trajano.ms.auth.test;

import net.trajano.ms.authz.ClientInfoResource;
import org.junit.Test;

import java.net.URI;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class UriTest {

    @Test
    public void testSimpleOrigin() throws Exception {

        assertEquals(URI.create("https://bugzilla.mozilla.org"), ClientInfoResource.getPartsForOriginHeader(new URL("https://bugzilla.mozilla.org")));
    }

    @Test
    public void testOriginWithSlashPath() throws Exception {

        assertEquals(URI.create("https://bugzilla.mozilla.org"), ClientInfoResource.getPartsForOriginHeader(new URL("https://bugzilla.mozilla.org/")));
    }

    @Test
    public void testOriginWithPath() throws Exception {

        assertEquals(URI.create("https://bugzilla.mozilla.org"), ClientInfoResource.getPartsForOriginHeader(new URL("https://bugzilla.mozilla.org/show_bug.cgi?id=446344#c97")));
    }

    @Test
    public void testOriginWithPort() throws Exception {

        assertEquals(URI.create("https://bugzilla.mozilla.org:3423"), ClientInfoResource.getPartsForOriginHeader(new URL("https://bugzilla.mozilla.org:3423/show_bug.cgi?id=446344#c97")));
    }
}
