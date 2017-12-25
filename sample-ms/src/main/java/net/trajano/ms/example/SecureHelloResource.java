package net.trajano.ms.example;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import net.trajano.ms.core.JsonOps;
import net.trajano.ms.core.JwtClaimsSetPrincipal;
import net.trajano.ms.example.beans.Counter;
import net.trajano.ms.example.beans.UselessCounter;

/**
 * This resource uses CDI rather than Spring annotations.
 *
 * @author Archimedes Trajano
 */
@Api(tags = {
    "infernal",
    "doom"
},
    authorizations = @Authorization("Bearer"))
@ApplicationScoped
@Path("/s")
public class SecureHelloResource {

    @Inject
    private Counter counter;

    @Inject
    private JsonOps jsonOps;

    @Inject
    private UselessCounter uselessCounter;

    @ApiOperation(value = "Counts")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/count")
    public JsonObject count() {

        final JsonObject json = new JsonObject();
        json.add("counter", new JsonPrimitive(counter.count()));
        json.add("useless_counter", new JsonPrimitive(uselessCounter.count()));
        return json;
    }

    @ApiOperation(value = "Secure Hello")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject hello(@QueryParam("name") final String name,
        @Context final SecurityContext securityContext) {

        final JsonObject json = new JsonObject();
        if (name != null) {
            json.add("name", new JsonPrimitive(name));
        }
        final JwtClaimsSetPrincipal userPrincipal = (JwtClaimsSetPrincipal) securityContext.getUserPrincipal();
        json.add("principal", new JsonPrimitive(userPrincipal.getName()));
        json.add("claims", jsonOps.toJsonElement(userPrincipal.getClaimsSet().toJson()));
        return json;
    }

    @ApiOperation(value = "displays hello world after a given amount of seconds seconds")
    @GET
    @Path("/suspend/{seconds}")
    @Produces(MediaType.TEXT_PLAIN)
    public void suspend(@Suspended final AsyncResponse asyncResponse,
        @PathParam("seconds") final int seconds) throws InterruptedException {

        Thread.sleep(seconds * 1000L);
        asyncResponse.resume(Response.ok("hello").build());
    }

}
