package net.trajano.ms.oidc.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;

@Api
@Component
@Path("/oidc")
public class OpenIdConnectResource {

    @Autowired
    private ServiceConfiguration serviceConfiguration;

    @Path("/auth")
    @GET
    public Response auth(@QueryParam("state") final String state,
        @QueryParam("issuer_id") final String issuerId,
        @Context final org.wso2.msf4j.Request req) {

        if (issuerId == null) {
            return Response.ok("Missing issuer_id").status(Status.BAD_REQUEST).build();
        }

        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            return Response.ok("Invalid issuer_id").status(Status.BAD_REQUEST).build();
        }
        return Response.ok().status(Status.TEMPORARY_REDIRECT).header("Location", issuerConfig.buildAuthenticationRequestUri(serviceConfiguration.getRedirectUri(), state)).build();
    }

    @Path("/auth-uri")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response authUri(@QueryParam("state") final String state,
        @QueryParam("issuer_id") final String issuerId,
        @Context final org.wso2.msf4j.Request req) {

        if (issuerId == null) {
            return Response.ok("Missing issuer_id").status(Status.BAD_REQUEST).build();
        }

        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            return Response.ok("Invalid issuer_id").status(Status.BAD_REQUEST).build();
        }
        return Response.ok(issuerConfig.buildAuthenticationRequestUri(serviceConfiguration.getRedirectUri(), state)).build();
    }

}
