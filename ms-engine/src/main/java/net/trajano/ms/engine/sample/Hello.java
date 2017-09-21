package net.trajano.ms.engine.sample;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.vertx.core.Vertx;

@Api
@Path("/hello")
public class Hello {

    @Inject
    private ISomeAppScope scoped;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@Context final io.vertx.core.Context vertxContext,
        @Context final Vertx vertx) {

        return "Hello" + scoped.get() + " " + vertx + " " + vertxContext;
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
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloHello() {

        return "HelloHello";
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String helloHelloPost(@FormParam("me") final String me) {

        return "HelloHello " + me;
    }

    @PostConstruct
    public void init() {

        System.out.println("INIT");
    }
}
