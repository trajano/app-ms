package net.trajano.ms.gateway.tokenendpoint;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.trajano.ms.common.JwtNotRequired;
import net.trajano.ms.gateway.OAuthTokenResponse;

/**
 * This endpoint acts as a OAuth token endpoint that would take an existing JWT
 * from the Identity Provider and provide an OAuth token response that is used
 * within the application.
 *
 * @author Archimedes Trajano
 */
@Path("/token")
@JwtNotRequired
public class TokenResource {

    /**
     * This performs a check whether the given client is authorized. It will throw a
     * {@link BadRequestException} with unauthorized_client if it fails.
     *
     * @param grantType
     * @param clientId
     * @param clientSecret
     */
    private void checkClientAuthorized(final String grantType,
        final String clientId,
        final String clientSecret) {

        if (false) {
            final OAuthTokenResponse r = new OAuthTokenResponse();
            r.setError("unauthorized_client");
            throw new BadRequestException(Response.ok(r).build());
        }

    }

    private BadRequestException invalidGrant() {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError("invalid_grant");
        return new BadRequestException(Response.ok(r).build());
    }

    /**
     * This will process a the "urn:ietf:params:oauth:grant-type:jwt-bearer" grant.
     * This will provide an OAuth token response containing the the access_token
     * used by the application.
     *
     * @param assertion
     *            the JWT token.
     * @return OAuth token
     */
    private OAuthTokenResponse processJwtBearer(final String assertion) {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setAccessToken("sometoken");
        return r;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response token(@FormParam("grant_type") final String grantType,
        @FormParam("client_id") final String clientId,
        @FormParam("client_secret") final String clientSecret,
        final MultivaluedMap<String, String> form) {

        checkClientAuthorized(grantType, clientId, clientSecret);

        if ("urn:ietf:params:oauth:grant-type:jwt-bearer".equals(grantType)) {
            return Response.ok(processJwtBearer(form.getFirst("assertion"))).build();
        } else if ("authorization_code".equals(grantType)) {
            // TODO return new
            return null;
        } else if ("refresh_token".equals(grantType)) {
            // TODO return new
            return null;
        }
        throw unsupportedGrantType();
    }

    private BadRequestException unsupportedGrantType() {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError("unsupported_grant_type");
        return new BadRequestException(Response.ok(r).build());
    }

}
