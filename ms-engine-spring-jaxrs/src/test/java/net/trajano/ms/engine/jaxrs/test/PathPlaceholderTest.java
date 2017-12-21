package net.trajano.ms.engine.jaxrs.test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import net.trajano.ms.engine.jaxrs.JaxRsPath;

public class PathPlaceholderTest {

    @Test
    public void testExact() {

        final JaxRsPath path = new JaxRsPath("/exact", new String[0], new String[0], HttpMethod.POST);
        assertTrue(path.isExact());
        assertEquals(path.getPath(), path.getPathRegex());
    }

    @Test
    public void testOrder() {

        final JaxRsPath path1 = new JaxRsPath("/exact/{id}/foo/{x}", new String[0], new String[0], HttpMethod.PUT);
        final JaxRsPath path2 = new JaxRsPath("/exact/{id}/foo", new String[0], new String[0], HttpMethod.PUT);
        final JaxRsPath path3 = new JaxRsPath("/exact/abc/foo", new String[0], new String[0], HttpMethod.PUT);
        final JaxRsPath path4 = new JaxRsPath("/exact/abc/foo/{x}", new String[0], new String[0], HttpMethod.PUT);

        final List<JaxRsPath> list = Arrays.asList(path1, path2, path3, path4);
        Collections.sort(list);
        assertEquals(Arrays.asList(path3, path1, path2, path4), list);

    }

    @Test
    public void testRegexPlaceholder() {

        final JaxRsPath path = new JaxRsPath("/exact/{id}/foo/{x: [abc][0-9]+}", null, new String[0], HttpMethod.GET);
        assertFalse(path.isExact());
        assertEquals("/exact/[^/]+/foo/[abc][0-9]+", path.getPathRegex());
    }

    @Test
    public void testSimplePlaceholder() {

        final JaxRsPath path = new JaxRsPath("/exact/{id}", new String[0], new String[0], HttpMethod.POST);
        assertFalse(path.isExact());
        assertEquals("/exact/[^/]+", path.getPathRegex());
    }

    @Test
    public void testSimplePlaceholder2() {

        final JaxRsPath path = new JaxRsPath("/exact/{id}/foo", new String[0], new String[0], HttpMethod.POST);
        assertFalse(path.isExact());
        assertEquals("/exact/[^/]+/foo", path.getPathRegex());
    }

    @Test
    public void testSimplePlaceholder3() {

        final JaxRsPath path = new JaxRsPath("/exact/{id}/foo/{x}", new String[0], new String[0], HttpMethod.PUT);
        assertFalse(path.isExact());
        assertEquals("/exact/[^/]+/foo/[^/]+", path.getPathRegex());
    }
}
