package net.trajano.ms.vertx.test;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.sample.MyApp;
import net.trajano.ms.spi.MicroserviceEngine;
import net.trajano.ms.vertx.VertxConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    VertxConfig.class
})
public class SpringJaxrsHandlerTest {

    @BeforeClass
    public static void setApplication() {

        MicroserviceTestUtil.setApplicationClass(MyApp.class);
    }

    @Autowired
    private MicroserviceEngine engine;

    @Test
    public void testEngine() {

        assertNotNull(engine);
    }

}
