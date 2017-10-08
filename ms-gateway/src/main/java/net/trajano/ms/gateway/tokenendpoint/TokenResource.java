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
import net.trajano.ms.gateway.IdTokenResponse;
import net.trajano.ms.gateway.OAuthTokenResponse;

/**
 * This endpoint acts as a OAuth token endpoint that would take an existing JWT
 * from the Identity Provider and provide an OAuth token response that is used
 * within the application.
 * <p>
 * This endpoint should not be exposed outside of the gateway and only the
 * gateway or the OIDC callback provider should be calling this endpoint.
 *
 * @author Archimedes Trajano
 */
@Path("/token")
@JwtNotRequired
public class TokenResource {

    /**
     * This performs a check whether the given client is authorized. It will
     * throw a {@link BadRequestException} with unauthorized_client if it fails.
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
     * This will process the "authorization_code" grant. This will return a new
     * {@link IdTokenResponse} that contains the claims that are used within the
     * application behind the gateway.
     * <p>
     * If there is an authentication failure then {@link #invalidGrant()} is
     * thrown.
     *
     * @param accessToken
     *            the access token
     * @return ID Token response
     */
    private IdTokenResponse processAuthorizationCode(final String accessToken) {

        if (false) {
            throw invalidGrant();
        }
        return null;
    }

    /**
     * This will process the "urn:ietf:params:oauth:grant-type:jwt-bearer"
     * grant. This will provide an OAuth token response containing the the
     * access_token used by the application.
     * <p>
     * If there is an authentication failure then {@link #invalidGrant()} is
     * thrown.
     *
     * @param assertion
     *            the JWT token.
     * @return OAuth token
     */
    private OAuthTokenResponse processJwtBearer(final String assertion) {

        if (false) {
            throw invalidGrant();
        }
        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setAccessToken("sometoken");
        return r;
    }

    /**
     * This will process the "refresh_token" grant. This will provide an updated
     * OAuth token response containing the the access_token used by the
     * application.
     * <p>
     * If there is an authentication failure then {@link #invalidGrant()} is
     * thrown.
     *
     * @param refreshToken
     *            the refresh token.
     * @return OAuth token
     */
    private OAuthTokenResponse processRefreshToken(final String refreshToken) {

        if (false) {
            throw invalidGrant();
        }
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
            return Response.ok(processAuthorizationCode(form.getFirst("authorization_code"))).build();
        } else if ("refresh_token".equals(grantType)) {
            return Response.ok(processRefreshToken(form.getFirst("refresh_token"))).build();
        }
        throw unsupportedGrantType();
    }

    private BadRequestException unsupportedGrantType() {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError("unsupported_grant_type");
        return new BadRequestException(Response.ok(r).build());
    }

}
