package net.trajano.ms.oidc.internal;

import io.swagger.annotations.Api;
import net.trajano.ms.auth.token.ErrorCodes;
import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.token.IdTokenResponse;
import net.trajano.ms.common.oauth.OAuthTokenResponse;
import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.oidc.OpenIdConfiguration;
import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.net.URI;

@Api
@Component
@Path("/oidc")
public class OpenIdConnectResource {

    @Autowired
    private Client client;

    @Value("${authorizationEndpoint}")
    private URI authorizationEndpoint;

    @Autowired
    private CryptoOps cryptoOps;

    @Autowired
    private ServiceConfiguration serviceConfiguration;

    @Path("/auth")
    @GET
    public Response auth(@QueryParam("state") final String state,
        @QueryParam("issuer_id") final String issuerId,
        @Context final UriInfo uriInfo) {

        return Response.ok().status(Status.TEMPORARY_REDIRECT).header("Location", authUri(state, issuerId, uriInfo)).build();
    }

    @Path("/auth-uri")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public URI authUri(@QueryParam("state") final String state,
        @QueryParam("issuer_id") final String issuerId,
        @Context final UriInfo uriInfo) {

        if (issuerId == null) {
            throw new BadRequestException("Missing issuer_id");
        }

        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            throw new BadRequestException("Invalid issuer_id");
        }
        final URI redirectUri = UriBuilder.fromUri(serviceConfiguration.getRedirectUri()).path(issuerId).build();
        final URI buildAuthenticationRequestUri = issuerConfig.buildAuthenticationRequestUri(redirectUri, state, cryptoOps.newToken());
        return buildAuthenticationRequestUri;
    }

    @Path("/cb/{issuer_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public OAuthTokenResponse callback(@QueryParam("code") final String code,
        @PathParam("issuer_id") final String issuerId) {

        if (issuerId == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Missing issuer_id");
        }
        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Invalid issuer_id");
        }

        final URI redirectUri = UriBuilder.fromUri(serviceConfiguration.getRedirectUri()).path(issuerId).build();
        final Form form = new Form();
        form.param("redirect_uri", redirectUri.toASCIIString());
        form.param("grant_type", "authorization_code");
        form.param("code", code);
        final OpenIdConfiguration openIdConfiguration = issuerConfig.getOpenIdConfiguration();
        final IdTokenResponse openIdToken = client.target(openIdConfiguration.getTokenEndpoint())
            .request(MediaType.APPLICATION_JSON)
            .header("Authorization", issuerConfig.buildAuthorization())
            .post(Entity.form(form), IdTokenResponse.class);

        final JwtClaims jwtClaims = cryptoOps.toClaimsSet(openIdToken.getIdToken(), openIdConfiguration.getHttpsJwks());

        final Form storeInternalForm = new Form();
        storeInternalForm.param("grant_type", GrantTypes.JWT_ASSERTION);
        storeInternalForm.param("client_id", "CLIENT_ID");
        storeInternalForm.param("assertion", cryptoOps.sign(jwtClaims));

        return client.target(authorizationEndpoint).request(MediaType.APPLICATION_JSON).post(Entity.form(storeInternalForm), OAuthTokenResponse.class);

    }

}
