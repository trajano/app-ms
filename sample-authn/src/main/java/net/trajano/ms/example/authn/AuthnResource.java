package net.trajano.ms.example.authn;

import java.net.URI;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;

import net.trajano.ms.core.NonceOps;
import net.trajano.ms.core.ErrorResponses;
import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.token.OAuthTokenResponse;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.core.ErrorResponse;

/**
 * This works like the FORM based login of Java EE. It allows any user name as
 * long as the password is "password"
 */
@SwaggerDefinition(
    info = @Info(
        title = "Sample Authn Microservice",
        version = "1.0"))
@Api
@Component
@Path("/authn")
@PermitAll
public class AuthnResource {

    @Value("${authorizationEndpoint}")
    private URI authorizationEndpoint;

    /**
     * configuration value to determine if the cookies should be sent as secure.
     * This is false when testing on non SSL hosts.
     */
    @Value("${secure:#{true}}")
    private boolean secure;

    @Context
    private Client client;

    @Autowired
    private CryptoOps cryptoOps;

    @Autowired
    private NonceOps nonceProvider;

    @GET
    @Path("/nonce")
    public Response getNonce() {

        return Response.ok()
            .cookie(new NewCookie("nonce", nonceProvider.newNonce(), null, null, null, NewCookie.DEFAULT_MAX_AGE, secure, true))
            .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(@ApiResponse(code = 401,
        message = "Unauthorized Response",
        response = ErrorResponse.class))
    public Response json(
        @FormParam("j_username") @ApiParam("User name") final String username,
        @FormParam("j_password") @ApiParam("Password") final String password,
        @CookieParam("nonce") final String nonce,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        if (!nonceProvider.claimNonce(nonce)) {
            throw ErrorResponses.invalidRequest("invalid nonce");
        }
        if (!"password".equals(password)) {
            throw ErrorResponses.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "invalid username/password combination", "FORM");
        }
        final JwtClaims claims = new JwtClaims();
        claims.setSubject(username);
        claims.setAudience(HttpAuthorizationHeaders.parseBasicAuthorization(authorization)[0]);

        final Form form = new Form();
        form.param("grant_type", GrantTypes.JWT_ASSERTION);
        form.param("assertion", cryptoOps.sign(claims));

        return Response.ok(client.target(authorizationEndpoint).request(MediaType.APPLICATION_JSON_TYPE)
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .post(Entity.form(form), OAuthTokenResponse.class))
            .cookie(new NewCookie("nonce", "", null, null, null, 0, secure, true))
            .build();

    }

}
