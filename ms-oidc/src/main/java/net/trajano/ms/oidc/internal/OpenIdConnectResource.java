package net.trajano.ms.oidc.internal;

import java.net.URI;

import javax.annotation.security.PermitAll;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.token.IdTokenResponse;
import net.trajano.ms.auth.token.OAuthTokenResponse;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.oidc.OpenIdConfiguration;

@Api
@Component
@Path("/oidc")
@PermitAll
public class OpenIdConnectResource {

    /**
     * Gateway client ID. The gateway has it's own client ID because it is the
     * only one that should be authorized to get the id_token from an
     * authorization_code request to the authorization server token endpoint.
     */
    @Value("${authorization.client_id}")
    private String appClientId;

    /**
     * Gateway client secret
     */
    @Value("${authorization.client_secret}")
    private String appClientSecret;

    @Value("${authorization.endpoint}")
    private URI authorizationEndpoint;

    @Context
    private Client client;

    @Autowired
    private CryptoOps cryptoOps;

    @Autowired
    private ServiceConfiguration serviceConfiguration;

    @Path("/auth/{issuer_id}")
    @GET
    public Response auth(@QueryParam("state") final String state,
        @PathParam("issuer_id") final String issuerId,
        @Context final UriInfo uriInfo) {

        return Response.ok().status(Status.TEMPORARY_REDIRECT).header("Location", authUri(state, issuerId, uriInfo)).build();
    }

    @Path("/auth-uri/{issuer_id}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public URI authUri(@QueryParam("state") final String state,
        @PathParam("issuer_id") final String issuerId,
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
            .header(HttpHeaders.AUTHORIZATION, issuerConfig.buildAuthorization())
            .post(Entity.form(form), IdTokenResponse.class);

        final JwtClaims jwtClaims = cryptoOps.toClaimsSet(openIdToken.getIdToken(), openIdConfiguration.getHttpsJwks());

        final Form storeInternalForm = new Form();
        storeInternalForm.param("grant_type", GrantTypes.JWT_ASSERTION);
        storeInternalForm.param("assertion", cryptoOps.sign(jwtClaims));
        storeInternalForm.param("aud", issuerConfig.getClientId());

        return client.target(authorizationEndpoint).request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, HttpAuthorizationHeaders.buildBasicAuthorization(appClientId, appClientSecret))
            .post(Entity.form(storeInternalForm), OAuthTokenResponse.class);

    }

}
