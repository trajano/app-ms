package net.trajano.ms.engine.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;

@Path("/")
public class ApiListingResource {

    @GET
    @Produces({
        MediaType.APPLICATION_JSON,
        "application/yaml"
    })
    public Response getSwagger(@Context final Application config,
        @Context final UriInfo uriInfo) throws JsonProcessingException {

        final BeanConfig beanConfig = new BeanConfig();

        beanConfig.setResourcePackage(((ResourceConfig) config).getApplication().getClass().getPackage().getName());
        beanConfig.setScan(true);
        beanConfig.setBasePath(uriInfo.getBaseUri().getPath());
        beanConfig.scanAndRead();
        Swagger swagger = beanConfig.getSwagger();
        return Response.ok().entity(swagger).build();

    }
}
