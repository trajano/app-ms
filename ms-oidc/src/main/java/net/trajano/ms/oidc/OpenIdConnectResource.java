package net.trajano.ms.oidc;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
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

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import io.swagger.annotations.Api;
import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.token.OAuthTokenResponse;
import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.core.ErrorResponses;
import net.trajano.ms.oidc.internal.AuthenticationUriBuilder;
import net.trajano.ms.oidc.internal.HazelcastConfiguration;
import net.trajano.ms.oidc.internal.ServerState;
import net.trajano.ms.oidc.spi.IssuerConfig;
import net.trajano.ms.oidc.spi.ServiceConfiguration;

@Api
@Component
@Path("/oidc")
@PermitAll
public class OpenIdConnectResource {

    /**
     * "id_token" key in the ID Token response.
     */
    private static final String ID_TOKEN = "id_token";

    private static final Logger LOG = LoggerFactory.getLogger(OpenIdConnectResource.class);

    @Autowired
    private AuthenticationUriBuilder authenticationUriBuilder;

    @Value("${authorization.endpoint}")
    private URI authorizationEndpoint;

    @Context
    private Client client;

    @Autowired
    private CacheManager cm;

    @Autowired
    private CryptoOps cryptoOps;

    private Cache serverStateCache;

    @Autowired
    private ServiceConfiguration serviceConfiguration;

    /**
     * The state that is passed here is transformed to a JWT before passing to the
     * OIDC IP.
     *
     * @param state
     *            this is a client level state
     * @param issuerId
     *            issuer
     * @return a redirect
     */
    @Path("/auth/{issuer_id}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response auth(@FormParam("state") final String state,
        @PathParam("issuer_id") final String issuerId,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        return Response.seeOther(authUri(state, issuerId, authorization)).build();
    }

    @Path("/auth-uri/{issuer_id}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public URI authUri(@QueryParam("state") final String state,
        @PathParam("issuer_id") final String issuerId,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        getRedirectUri(authorization);
        return authenticationUriBuilder.build(state, issuerId, authorization, new JwtClaims());
    }

    @Path("/auth-info/{issuer_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject authUriJson(@QueryParam("state") final String state,
        @PathParam("issuer_id") final String issuerId,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        final JsonObject uriObject = new JsonObject();
        uriObject.addProperty("uri", authUri(state, issuerId, authorization).toASCIIString());
        return uriObject;
    }

    @Path("/cb/{issuer_id}")
    @GET
    public Response callback(@QueryParam("code") final String code,
        @QueryParam("state") final String jwtState,
        @PathParam("issuer_id") final String issuerId) throws MalformedClaimException {

        if (issuerId == null) {
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Missing issuer_id");
        }
        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Invalid issuer_id");
        }

        final ServerState serverState = serverStateCache.get(jwtState, ServerState.class);
        if (serverState == null) {
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Invalid state");
        }

        final URI redirectUri = UriBuilder.fromUri(serviceConfiguration.getRedirectUri()).path(issuerId).build();
        final Form form = new Form();
        form.param("redirect_uri", redirectUri.toASCIIString());
        form.param("grant_type", GrantTypes.AUTHORIZATION_CODE);
        form.param("code", code);
        final OpenIdConfiguration openIdConfiguration = issuerConfig.getOpenIdConfiguration();
        final URI tokenEndpoint = openIdConfiguration.getTokenEndpoint();
        final Response clientResponse = client.target(tokenEndpoint)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, issuerConfig.buildAuthorization())
            .post(Entity.form(form));
        final JsonObject openIdToken = clientResponse.readEntity(JsonObject.class);
        if (clientResponse.getStatus() != Status.OK.getStatusCode()) {
            LOG.error("Received = {} from {}", openIdToken, tokenEndpoint);
            throw ErrorResponses.internalServerError("server unable to get id_token");
        }

        final JwtClaims idTokenClaims = cryptoOps.toClaimsSet(openIdToken.get(ID_TOKEN).getAsString(), openIdConfiguration.getHttpsJwks());
        if (!serverState.getNonce().equals(idTokenClaims.getStringClaimValue("nonce"))) {
            throw ErrorResponses.internalServerError("nonce did not match");
        }

        // Add additional claims but throw an error if it already exists.
        serverState.getAdditionalClaims().getClaimsMap()
            .forEach((k,
                v) -> {
                if (idTokenClaims.hasClaim(k)) {
                    throw new InternalServerErrorException("The claim " + k + " already exists from the IP");
                } else {
                    idTokenClaims.setClaim(k, v);
                }
            });

        final Form storeInternalForm = new Form();
        storeInternalForm.param("grant_type", GrantTypes.JWT_ASSERTION);
        storeInternalForm.param("assertion", cryptoOps.sign(idTokenClaims));
        storeInternalForm.param("aud", issuerConfig.getClientId());

        final OAuthTokenResponse tokenResponse = client.target(authorizationEndpoint).path("/token").request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, serverState.getClientCredentials())
            .post(Entity.form(storeInternalForm), OAuthTokenResponse.class);

        final URI newUri;
        final URI redirectUri1 = getRedirectUri(serverState.getClientCredentials());
        if (tokenResponse.isExpiring()) {
            newUri = UriBuilder
                .fromUri(redirectUri1)
                .fragment("state={state}&access_token={access_token}&refresh_token={refresh_token}&token_type={token_type}&expires_in={expires_in}")
                .build(serverState.getClientState(),
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getTokenType(),
                    tokenResponse.getExpiresIn());
        } else {
            newUri = UriBuilder
                .fromUri(redirectUri1)
                .fragment("state={state}&access_token={access_token}&refresh_token={refresh_token}&token_type={token_type}")
                .build(serverState.getClientState(),
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getTokenType());
        }
        return Response.temporaryRedirect(newUri).build();

    }

    /**
     * Gets the redirect URI from authorization endpoint.
     *
     * @param authorization
     *            authorization header
     * @return
     */
    private URI getRedirectUri(final String authorization) {

        try {
            LOG.debug("Obtaining redirect URI using authorization={}", authorization);
            return URI.create(client.target(authorizationEndpoint).path("/check/openid-redirect-uri").request(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .get(String.class));
        } catch (final BadRequestException e) {
            throw ErrorResponses.invalidAuthorization();
        }
    }

    @PostConstruct
    public void init() {

        serverStateCache = cm.getCache(HazelcastConfiguration.SERVER_STATE);
    }

    public void setClient(final Client client) {

        this.client = client;
    }
}
