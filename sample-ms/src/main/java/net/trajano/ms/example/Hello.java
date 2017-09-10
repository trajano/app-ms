package net.trajano.ms.example;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

@SwaggerDefinition(
    info = @Info(
        title = "Sample microservice from hello",
        version = "1.0"))
@Api
@Component
@Path("hello")
public class Hello {

    @ApiOperation(value = "displays hello world",
        authorizations = @Authorization("JWT"))
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String helloWorld() {

        return "Hello world at " + new Date();
    }
}
