package net.trajano.ms.engine.test;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.buffer.Buffer;
import net.trajano.ms.engine.internal.VertxBufferInputStream;

public class BufferTest {

    private byte[] test;

    @Before
    public void buildTestData() throws Exception {

        test = new byte[1024];
        SecureRandom.getInstanceStrong().nextBytes(test);
        for (int i = 0; i < 256; ++i) {
            test[i] = (byte) i;
        }

    }

    @Test
    public void testBlockingInputStream() throws Exception {

        final Buffer buff = Buffer.buffer(test);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new VertxBufferInputStream(buff), baos);
        Assert.assertArrayEquals(test, baos.toByteArray());

    }

    @Test
    public void testBuffer() {

        final Buffer buff = Buffer.buffer(test);
        Assert.assertArrayEquals(test, buff.getBytes());

    }

    @Test
    public void testBufferInputStream() throws Exception {

        final Buffer buff = Buffer.buffer(test);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new VertxBufferInputStream(buff), baos);
        Assert.assertArrayEquals(test, baos.toByteArray());

    }

}
