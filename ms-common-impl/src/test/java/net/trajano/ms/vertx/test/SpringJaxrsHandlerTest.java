package net.trajano.ms.vertx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

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
        final Response response = ClientBuilder.newClient().target("http://localhost:8900/api/sing").request().get();
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).startsWith("Hello"));

    }

}
