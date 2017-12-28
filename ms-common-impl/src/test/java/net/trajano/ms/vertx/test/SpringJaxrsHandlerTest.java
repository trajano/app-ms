package net.trajano.ms.vertx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.JsonObject;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import net.trajano.ms.engine.internal.resteasy.VertxClientEngine;
import net.trajano.ms.sample.MyApp;
import net.trajano.ms.spi.MicroserviceEngine;
import net.trajano.ms.vertx.VertxConfig;
import net.trajano.ms.vertx.jaxrs.GsonMessageBodyHandler;

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
    private URI baseUri;

    @Autowired
    private MicroserviceEngine engine;

    @Autowired
    private HttpClientOptions httpClientOptions;

    @Test
    public void testAsync() {

        assertNotNull(engine);
        final Response response = ClientBuilder.newClient().target(baseUri).path("/api/hello/async").request().get();
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("accounts.google.com"));

    }

    @Test
    public void testEngine() {

        assertNotNull(engine);
        final Response response = ClientBuilder.newClient().target(baseUri).path("/api/sing").request().get();
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).startsWith("Hello"));

    }

    @Test
    public void testEngineWithInjectedClient() {

        final HttpClient httpClient = Vertx.vertx().createHttpClient(httpClientOptions);
        final Client client = new ResteasyClientBuilder().httpEngine(new VertxClientEngine(httpClient)).build();

        final Response response = client.target(baseUri).path("/api/sing").request().get();
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

    @Test
    public void testEngineWithInjectedClientPost() {

        final HttpClient httpClient = Vertx.vertx().createHttpClient(httpClientOptions);
        final Client client = new ResteasyClientBuilder().httpEngine(new VertxClientEngine(httpClient))
            .register(GsonMessageBodyHandler.class).build();
        final Form xform = new Form();
        xform.param("userName", "ca1\\\\meowmix");
        xform.param("password", "mingnamulan");
        xform.param("state", "authenticate");
        xform.param("style", "xml");
        xform.param("xsl", "none");

        final JsonObject arsString = client.target("https://httpbin.org/post").request()
            .post(Entity.form(xform), JsonObject.class);
        assertEquals("xml", arsString.getAsJsonObject("form").get("style").getAsString());
    }

    @Test
    public void testEngineWithInjectedClientPost2() {

        final ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.start();
        final ResteasyProviderFactory providerFactory = deployment.getProviderFactory();
        final HttpClient httpClient = Vertx.vertx().createHttpClient(httpClientOptions);
        final Client client = new ResteasyClientBuilder()
            .providerFactory(providerFactory)
            .httpEngine(new VertxClientEngine(httpClient))
            .register(GsonMessageBodyHandler.class)
            .build();
        final Form xform = new Form();
        xform.param("userName", "ca1\\\\meowmix");
        xform.param("password", "mingnamulan");
        xform.param("state", "authenticate");
        xform.param("style", "xml");
        xform.param("xsl", "none");

        final Response response = client.target("https://httpbin.org/post").request(MediaType.APPLICATION_JSON)
            .post(Entity.form(xform), Response.class);
        assertFalse(response.getStringHeaders().isEmpty());
        System.out.println(response.getStringHeaders());
        assertFalse(response.getHeaders().isEmpty());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        assertTrue(response.hasEntity());
        final JsonObject arsString = response.readEntity(JsonObject.class);
        assertEquals("xml", arsString.getAsJsonObject("form").get("style").getAsString());
    }
}
