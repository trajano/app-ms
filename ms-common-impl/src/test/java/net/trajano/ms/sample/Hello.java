package net.trajano.ms.sample;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.vertx.ext.web.RoutingContext;

@Api
@Path("/hello")
public class Hello {

    private int count;

    @Context
    private Client jaxrsClient;

    @Autowired
    SomeRequestScope req;

    @Autowired
    ISomeAppScope scope;

    @ApiOperation(value = "displays openid config of google async",
        hidden = true)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/async")
    @PermitAll
    public void async(@Suspended final AsyncResponse asyncResponse) throws InterruptedException,
        ExecutionException {

        final Future<Response> futureResponseFromClient = jaxrsClient.target("https://accounts.google.com/.well-known/openid-configuration").request().header(javax.ws.rs.core.HttpHeaders.USER_AGENT, "curl/7.55.1").async().get();

        final Response responseFromClient = futureResponseFromClient.get();
        try {
            final String object = responseFromClient.readEntity(String.class);
            asyncResponse.resume(object);
        } finally {
            responseFromClient.close();
        }
    }

    @GET
    @Path("/cough")
    public String cough() {

        throw new RuntimeException("ahem");
    }

    @GET
    @Produces("text/plain")
    @Path("/count")
    public Integer getCount() {

        return ++count;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public String hello(
        @Context final RoutingContext context) {

        return "Hello" + this + " " + scope + " " + context + " " + req;
        /*
         * @Context final Vertx vertx,
         * @Context final RoutingContext routingContext
         */
        //+ scoped.get()
        //@Context final io.vertx.core.Context vertxContext,
        //  + routingContext;//+ " " + vertx + " " + vertx.getOrCreateContext() + " " + routingContext;
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Blah hello2B() {

        return new Blah();
    }

    @GET
    @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    public Blah helloB() {

        return new Blah();
    }
}
