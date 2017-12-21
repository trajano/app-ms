package net.trajano.ms.engine.test;

import net.trajano.ms.engine.internal.BufferUtil;
import net.trajano.ms.engine.internal.Conversions;
import org.junit.Test;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;

public class UtilityClassTest {

    @Test
    public void testUtils() throws Exception {

        assertUtilityClassWellDefined(BufferUtil.class);
        assertUtilityClassWellDefined(Conversions.class);
    }
}
