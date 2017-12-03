package net.trajano.ms.vertx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import net.trajano.ms.engine.internal.resteasy.VertxClientEngine;
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

    @Autowired
    private HttpClientOptions httpClientOptions;

    @Test
    public void testEngine() {

        assertNotNull(engine);
        final Response response = ClientBuilder.newClient().target("http://localhost:8900/api/sing").request().get();
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).startsWith("Hello"));

    }

    @Test
    public void testEngineWithInjectedClient() {

        final HttpClient httpClient = Vertx.vertx().createHttpClient(httpClientOptions);
        final Client client = new ResteasyClientBuilder().httpEngine(new VertxClientEngine(httpClient)).build();

        final Response response = client.target("http://localhost:8900/api/sing").request().get();
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).startsWith("Hello"));

    }

    @Test
    public void testEngineWithInjectedClient400() {

        final HttpClient httpClient = Vertx.vertx().createHttpClient(httpClientOptions);
        final Client client = new ResteasyClientBuilder().httpEngine(new VertxClientEngine(httpClient)).build();

        final Response response = client.target("https://httpbin.org/status/400").request().get();
        assertEquals(400, response.getStatus());

    }

    @Test(expected = BadRequestException.class)
    public void testEngineWithInjectedClient400ViaException() {

        final HttpClient httpClient = Vertx.vertx().createHttpClient(httpClientOptions);
        final Client client = new ResteasyClientBuilder().httpEngine(new VertxClientEngine(httpClient)).build();

        client.target("https://httpbin.org/status/400").request().get(Map.class);

    }
}
