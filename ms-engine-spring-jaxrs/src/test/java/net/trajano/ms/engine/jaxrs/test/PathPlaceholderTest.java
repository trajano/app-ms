package net.trajano.ms.engine.jaxrs.test;

import net.trajano.ms.engine.jaxrs.JaxRsPath;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class PathPlaceholderTest {

    @Test
    public void testExact() {

        JaxRsPath path = new JaxRsPath("/exact", new String[0], new String[0], true, true, true, true);
        assertTrue(path.isExact());
        assertEquals(path.getPath(), path.getPathRegex());
    }

    @Test
    public void testSimplePlaceholder() {

        JaxRsPath path = new JaxRsPath("/exact/{id}", new String[0], new String[0], true, true, true, true);
        assertFalse(path.isExact());
        assertEquals("/exact/[^/]+", path.getPathRegex());
    }

    @Test
    public void testSimplePlaceholder2() {

        JaxRsPath path = new JaxRsPath("/exact/{id}/foo", new String[0], new String[0], true, true, true, true);
        assertFalse(path.isExact());
        assertEquals("/exact/[^/]+/foo", path.getPathRegex());
    }

    @Test
    public void testSimplePlaceholder3() {

        JaxRsPath path = new JaxRsPath("/exact/{id}/foo/{x}", new String[0], new String[0], true, true, true, true);
        assertFalse(path.isExact());
        assertEquals("/exact/[^/]+/foo/[^/]+", path.getPathRegex());
    }

    @Test
    public void testRegexPlaceholder() {

        JaxRsPath path = new JaxRsPath("/exact/{id}/foo/{x: [abc][0-9]+}", new String[0], new String[0], true, true, true, true);
        assertFalse(path.isExact());
        assertEquals("/exact/[^/]+/foo/[abc][0-9]+", path.getPathRegex());
    }
}
