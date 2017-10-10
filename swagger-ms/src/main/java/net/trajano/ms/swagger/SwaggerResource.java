package net.trajano.ms.swagger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.models.Swagger;
import net.trajano.ms.swagger.internal.SwaggerCollator;

@Path("/")
public class SwaggerResource {

    @Autowired
    private SwaggerCollator collator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{version}")
    public Swagger swagger(@PathParam("version") final String version) {

        return collator.getSwagger(version);
    }

}
