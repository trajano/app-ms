package net.trajano.ms.engine.test;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import net.trajano.ms.engine.internal.resteasy.VertxClientEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class VertxClientTest {

    @Test
    public void testWellKnown() {

        final Vertx vertx = Vertx.vertx(new VertxOptions().setAddressResolverOptions(new AddressResolverOptions().setMaxQueries(10)));

        final HttpClientOptions options = new HttpClientOptions()
            .setPipelining(true);
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
