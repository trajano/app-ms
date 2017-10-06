package net.trajano.ms.engine.test;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.ProxyOptions;
import net.trajano.ms.engine.internal.resteasy.VertxClientEngine;

public class VertxClientTest {

    @Test
    public void testWellKnown() {

        final Vertx vertx = Vertx.vertx();

        final HttpClientOptions options = new HttpClientOptions()
            .setPipelining(true)
            .setProxyOptions(new ProxyOptions()
                .setHost("204.40.130.129")
                .setPort(3128));
        final HttpClient httpClient = vertx.createHttpClient(options);
        final Client client = new ResteasyClientBuilder().httpEngine(new VertxClientEngine(httpClient)).build();
        String entity;
        {
            final Response response = client.target("https://accounts.google.com/.well-known/openid-configuration").request().get();
            entity = response.readEntity(String.class);
            response.close();
        }
        {
            final Response response = client.target("https://accounts.google.com/.well-known/openid-configuration").request().get();
            assertEquals(entity, response.readEntity(String.class));
            response.close();
        }
        {
            final Response response = client.target("https://accounts.google.com/.well-known/openid-configuration").request().get();
            assertEquals(entity, response.readEntity(String.class));
            response.close();
        }
        client.close();
        httpClient.close();
    }
}
