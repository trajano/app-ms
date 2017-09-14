package net.trajano.ms.common.test;

import java.util.jar.Manifest;

import org.junit.Assert;
import org.junit.Test;

import net.trajano.ms.common.internal.BuildInfoResource;

public class ManiftestTest {

    @Test
    public void testManifest() throws Exception {

        final Manifest mf = new Manifest(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"));
        Assert.assertTrue(mf.getMainAttributes().size() > 0);
    }

    @Test
    public void testManifestFromResource() throws Exception {

        final BuildInfoResource buildInfoResource = new BuildInfoResource();
        buildInfoResource.init();
        Assert.assertNotNull(buildInfoResource.getManifestAsJson());
        Assert.assertNotNull(buildInfoResource.getManifest());
    }
}
