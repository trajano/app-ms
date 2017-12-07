package net.trajano.ms.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mockito;

import net.trajano.ms.core.NonceObject;
import net.trajano.ms.core.NonceOps;

public class NonceOpTest {

    private abstract class Dummy implements
        NonceOps {
    }

    @Test
    public void testNonceObject() {

        final NonceOps ops = Mockito.mock(Dummy.class);
        Mockito.when(ops.newNonce()).thenReturn("abcd");
        Mockito.when(ops.newNonceObject()).thenCallRealMethod();
        final NonceObject newNonceObject = ops.newNonceObject();
        assertNotNull(newNonceObject);
        assertEquals("abcd", newNonceObject.getNonce());
    }
}
