package net.trajano.ms.engine.jaxrs;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import net.trajano.ms.engine.internal.resteasy.VertxClientEngine;

@Provider
public class JaxRsClientResolver implements
    ContextResolver<Client> {

    @Context
    private HttpClientOptions httpClientOptions;

    @Context
    private Vertx vertx;

    @Override
    public Client getContext(final Class<?> type) {

        Client jaxRsClient = (Client) vertx.getOrCreateContext().get(Client.class.getName());
        if (jaxRsClient == null) {
            final HttpClient httpClient = vertx.createHttpClient(httpClientOptions);
            jaxRsClient = new ResteasyClientBuilder().providerFactory(providerFactory).httpEngine(new VertxClientEngine(httpClient)).build();
            vertx.getOrCreateContext().put(Client.class.getName(), jaxRsClient);
        }
        return jaxRsClient;
    }
}
