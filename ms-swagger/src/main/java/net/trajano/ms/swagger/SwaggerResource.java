package net.trajano.ms.swagger;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import io.swagger.models.Swagger;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.common.JwtNotRequired;
import net.trajano.ms.swagger.internal.SwaggerCollator;

@Api
@Path("/")
@JwtNotRequired
public class SwaggerResource {

    @Autowired
    private SwaggerCollator collator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{version}")
    public Swagger swagger(@PathParam("version") final String version,
        @Context final RoutingContext routingContext) {

        if (!collator.isPathExists(version)) {
            throw new NotFoundException();
        }
        return collator.getSwagger(version, routingContext);
    }
}
