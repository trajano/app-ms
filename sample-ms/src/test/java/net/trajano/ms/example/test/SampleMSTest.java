package net.trajano.ms.example.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.engine.internal.spring.SpringConfiguration;
import net.trajano.ms.engine.jaxrs.CommonObjectMapperProvider;
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
    private SampleMS sampleMs;

    @Test
    public void test() {

        assertNotNull(sampleMs);
    }
}
