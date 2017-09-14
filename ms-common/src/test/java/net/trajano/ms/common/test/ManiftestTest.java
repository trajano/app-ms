package net.trajano.ms.common.test;

import java.util.jar.Manifest;

import org.junit.Assert;
import org.junit.Test;

public class ManiftestTest {

    @Test
    public void testManifest() throws Exception {

        final Manifest mf = new Manifest(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"));
        Assert.assertTrue(mf.getMainAttributes().size() > 0);
    }
}
