package net.trajano.ms.engine.sample;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.ext.web.RoutingContext;

@Path("/hello")
@Component
public class Hello {

    private static final Logger LOG = LoggerFactory.getLogger(Hello.class);

    @Autowired
    ISomeAppScope scope;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/cough")
    public String cough(@Context final RoutingContext routingContext) {

        throw new RuntimeException("ahem");
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@Context final RoutingContext routingContext) {

        return "Hello" + this + " " + scope + " " + routingContext;
        /*
         * @Context final Vertx vertx,
         * @Context final RoutingContext routingContext
         */
        //+ scoped.get()
        //@Context final io.vertx.core.Context vertxContext,
        //  + routingContext;//+ " " + vertx + " " + vertx.getOrCreateContext() + " " + routingContext;
    }

}
