package net.trajano.ms.example.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.engine.internal.spring.SpringConfiguration;
import net.trajano.ms.engine.jaxrs.CommonObjectMapperProvider;
import net.trajano.ms.example.HelloResource;
import net.trajano.ms.example.SampleMS;
import net.trajano.ms.vertx.beans.GsonJacksonJsonOps;
import net.trajano.ms.vertx.beans.GsonProvider;

/**
 * This tests that all the autowired components and configuration are set up
 * correctly so that the application would actually start up.
 *
 * @author Archimedes Trajano
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    GsonJacksonJsonOps.class,
    GsonProvider.class,
    CommonObjectMapperProvider.class,
    SpringConfiguration.class,
    SampleMS.class
})
public class SampleMSTest {

    @Autowired
    private HelloResource helloResource;

    @Autowired
    private SampleMS sampleMs;

    @Test
    public void test() {

        assertNotNull(sampleMs);
        assertNotNull(helloResource);

    }

    @Test
    public void testStreaming() throws Exception {

        final Response response = helloResource.streamLorem();
        assertTrue(response.getEntity() instanceof StreamingOutput);
        final StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();
        streamingOutput.write(NullOutputStream.NULL_OUTPUT_STREAM);

    }
}
