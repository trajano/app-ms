package net.trajano.ms.example;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
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
        json.add("principal", new JsonPrimitive(securityContext.getUserPrincipal().getName()));
        return json;
    }

}
