package net.trajano.ms.example;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
}
