package net.trajano.ms.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.util.Objects;

import org.junit.Test;

import io.vertx.core.buffer.Buffer;
import net.trajano.ms.engine.internal.Symbol;

public class SymbolTest {

    @Test
    public void testSymbol() {

        final Buffer a = Symbol.newSymbol(Buffer.class);
        final Buffer b = Symbol.newSymbol(Buffer.class);
        assertFalse(a == b);
        assertEquals(a, a);
        assertEquals(b, b);
        assertEquals(b.hashCode(), b.hashCode());
        assertEquals(a.toString(), a.toString());
        assertFalse(Objects.equals(a, b));
    }

    @Test
    public void testSymbolWithAbstractClass() {

        final InputStream a = Symbol.newSymbol(InputStream.class);
        final InputStream b = Symbol.newSymbol(InputStream.class);
        assertFalse(a == b);
        assertEquals(a, a);
        assertEquals(b, b);
        assertEquals(b.hashCode(), b.hashCode());
        assertEquals(a.toString(), a.toString());
        assertFalse(Objects.equals(a, b));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedMethod() {

        final Buffer a = Symbol.newSymbol(Buffer.class);
        a.getByteBuf();
    }

}
