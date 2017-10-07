package net.trajano.ms.example;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.vertx.core.json.JsonObject;
import net.trajano.ms.common.JwtNotRequired;

@SwaggerDefinition(
    info = @Info(
        title = "Sample microservice from hello",
        version = "1.0"))
@Api
@Component
@Path("/hello")
@JwtNotRequired
public class Hello {

    @Context
    private Client jaxrsClient;

    @ApiOperation(value = "displays openid config of google async")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/async")
    public void async(@Suspended final AsyncResponse asyncResponse) throws Exception {

        final Future<Response> futureResponseFromClient = jaxrsClient.target("https://accounts.google.com/.well-known/openid-configuration").request().header(javax.ws.rs.core.HttpHeaders.USER_AGENT, "curl/7.55.1").async().get();

        final Response responseFromClient = futureResponseFromClient.get();
        try {
            final String object = responseFromClient.readEntity(String.class);
            asyncResponse.resume(object);
        } finally {
            responseFromClient.close();
        }
    }

    @ApiOperation(value = "displays openid config of google async")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/async2")
    public Response async2() throws Exception {

        final Future<Response> futureResponseFromClient = jaxrsClient.target("https://accounts.google.com/.well-known/openid-configuration").request().header(javax.ws.rs.core.HttpHeaders.USER_AGENT, "curl/7.55.1").accept(MediaType.APPLICATION_JSON).async().get();

        final Response responseFromClient = futureResponseFromClient.get();
        try {
            final JsonObject object = responseFromClient.readEntity(JsonObject.class);
            return Response.ok(object.toString()).build();
        } finally {
            responseFromClient.close();
        }
    }

    @ApiOperation(value = "throws an exception")
    @GET
    @Path("/cough")
    public Response cough() {

        throw new RuntimeException("ahem");
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String helloHelloPost(@FormParam("me") final String me) {

        return "HelloHello " + me;
    }

    @ApiOperation(value = "displays hello world")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String helloWorld() {

        return "Hello world at " + new Date();
    }

    @ApiOperation(value = "displays hello world")
    @GET
    @Path("/o")
    @Produces({
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    })
    public MyType helloWorld2() {

        final MyType myType = new MyType();
        myType.setFoo("Hello world at " + new Date());
        return myType;
    }

    @ApiOperation(value = "displays hello world")
    @GET
    @Path("/j")
    @Produces({
        MediaType.APPLICATION_JSON
    })
    public JsonObject json(@QueryParam("who") final String who) {

        return new JsonObject().put("who", who);
    }

    @ApiOperation(value = "displays openid config of google")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/openid")
    public Response maven() {

        final Response clientResponse = jaxrsClient.target("https://accounts.google.com/.well-known/openid-configuration").request().header(javax.ws.rs.core.HttpHeaders.USER_AGENT, "curl/7.55.1").get();
        try {
            final String value = clientResponse.readEntity(String.class);
            return Response.ok(value).build();
        } finally {
            clientResponse.close();
        }
    }

    @ApiOperation(value = "Cancelling after 1 seconds")
    @GET
    @Path("/s1")
    @Produces(MediaType.TEXT_PLAIN)
    public void s1(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        asyncResponse.setTimeout(5, TimeUnit.SECONDS);
        Thread.sleep(1000);
        asyncResponse.cancel(5);
    }

    @ApiOperation(value = "Timeout after 2 seconds")
    @GET
    @Path("/st")
    @Produces(MediaType.TEXT_PLAIN)
    public void st(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        asyncResponse.setTimeout(1, TimeUnit.SECONDS);
        Thread.sleep(2000);
        asyncResponse.resume(Response.ok("hello").build());
    }

    @ApiOperation(value = "displays hello world after 5 seconds")
    @GET
    @Path("/suspend")
    @Produces(MediaType.TEXT_PLAIN)
    public void suspend(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        Thread.sleep(2000);
        asyncResponse.resume(Response.ok("hello").build());
    }

}
