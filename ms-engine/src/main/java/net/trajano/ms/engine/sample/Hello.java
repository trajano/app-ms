package net.trajano.ms.engine.sample;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class Hello {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {

        return "Hello";
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloHello() {

        return "HelloHello";
    }

    @PostConstruct
    public void init() {

        System.out.println("INIT");
    }
}
