package net.trajano.ms.oidc.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wso2.msf4j.client.MSF4JClient;
import org.wso2.msf4j.client.exception.RestServiceException;

import io.swagger.annotations.Api;
import net.trajano.ms.oidc.IssuerConfig;
import net.trajano.ms.oidc.IssuersConfig;

@Api
@Component
@Path("/oidc")
public class OpenIdConnectResource {

    @Autowired
    private IssuersConfig issuersConfig;

    @Path("/auth-uri")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response authUri(@QueryParam("state") final String state,
        @QueryParam("issuer_id") final String issuerId) {

        if (issuerId == null) {
            return Response.ok("Missing issuer_id").status(Status.BAD_REQUEST).build();
        }

        final IssuerConfig issuerConfig = issuersConfig.getIssuer(issuerId);
        if (issuerConfig == null) {
            return Response.ok("Invalid issuer_id").status(Status.BAD_REQUEST).build();
        }
        try {
            System.out.println(new MSF4JClient.Builder<WellKnownAPI>().serviceEndpoint(issuerConfig.getUri().toASCIIString()).build().api().openIdConfiguration());
        } catch (final RestServiceException e) {
            e.printStackTrace();
        }
        //        final HttpGet getConfig = new HttpGet(issuerConfig.getUri().resolve(".well-known/openid-config"));

        //
        //        final OpenIdConfiguration openIdConfiguration = ClientBuilder.newClient().target(issuerConfig.getUri()).path(".well-known/openid-configuration").request(MediaType.APPLICATION_JSON).get(OpenIdConfiguration.class);
        //        issuerConfig.getUri()
        //        HttpClients.createDefault().execute(request)
        //ClientBuilder.newBuilder();
        return Response.ok("Hello world" + issuersConfig.getIssuer(issuerId)).build();
    }
}
