package net.trajano.ms.gateway;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * This defines the API endpoint
 *
 * @author
 */
@Api
@Path("/v1")
public class ApiV1 extends Router {

    @Path("/{path:.*}")
    public Response hello() {

        // routeTo
        return null;//routeTo("https://foo.com/")
    }

}
