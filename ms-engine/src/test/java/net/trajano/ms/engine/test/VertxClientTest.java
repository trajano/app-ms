package net.trajano.ms.engine.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Test;

import io.vertx.core.Vertx;
import net.trajano.ms.engine.internal.resteasy.VertxClientEngine;

public class VertxClientTest {

    @Test
    public void testWellKnown() {

        final Vertx vertx = Vertx.vertx();

        final Client client = new ResteasyClientBuilder().httpEngine(new VertxClientEngine(vertx)).build();
        final Response response = client.target("https://accounts.google.com/.well-known/openid-configuration").request().get();
        System.out.println(response.readEntity(String.class));
        response.close();
        client.close();
    }
}
