package net.trajano.ms.example;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @ApiOperation(value = "throws an exception")
    @GET
    @Path("/cough")
    public Response cough() {

        throw new RuntimeException("ahem");
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

    @ApiOperation(value = "displays hello world after 5 seconds")
    @GET
    @Path("/s1")
    @Produces(MediaType.TEXT_PLAIN)
    public void s1(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        asyncResponse.setTimeout(5, TimeUnit.SECONDS);
        Thread.sleep(1000);
        asyncResponse.cancel(5);
    }

    @ApiOperation(value = "displays hello world after 5 seconds")
    @GET
    @Path("/st")
    @Produces(MediaType.TEXT_PLAIN)
    public void st(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        asyncResponse.setTimeout(1, TimeUnit.SECONDS);
        Thread.sleep(2000);
        asyncResponse.cancel(5);
    }

    @ApiOperation(value = "displays hello world after 5 seconds")
    @GET
    @Path("/suspend")
    @Produces(MediaType.TEXT_PLAIN)
    public void suspend(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        asyncResponse.setTimeout(5, TimeUnit.SECONDS);
        Thread.sleep(2000);
        asyncResponse.resume(Response.ok("hello").build());
    }

}
