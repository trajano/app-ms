package net.trajano.ms.engine.sample;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.second.Blah;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/maven")
    public Response maven() {

        final Response clientResponse = jaxrsClient.target("http://search.maven.org/solrsearch/select?q=g:%22com.google.inject%22&rows=20&wt=json").request().header(javax.ws.rs.core.HttpHeaders.USER_AGENT, "curl/7.55.1").get();
        try {
            final String value = clientResponse.readEntity(String.class);
            return Response.ok(value).build();
        } finally {
            clientResponse.close();
        }
    }
}
