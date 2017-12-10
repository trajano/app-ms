package net.trajano.ms.example.authn;

import java.net.URI;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
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
import net.trajano.ms.core.ErrorResponses;

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

    @Context
    private Client client;

    @Autowired
    private CryptoOps cryptoOps;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(@ApiResponse(code = 401,
        message = "Unauthorized Response",
        response = ErrorResponse.class))
    public Response json(final UsernamePassword usernamePassword,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        if (!"password".equals(usernamePassword.getPassword())) {
            throw ErrorResponses.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "invalid username/password combination", "FORM");
        }

        final JwtClaims claims = new JwtClaims();
        claims.setSubject(usernamePassword.getUsername());
        claims.setAudience(HttpAuthorizationHeaders.parseBasicAuthorization(authorization)[0]);

        final Form form = new Form();
        form.param("grant_type", GrantTypes.JWT_ASSERTION);
        form.param("assertion", cryptoOps.sign(claims));

        return Response.ok(client.target(authorizationEndpoint).request(MediaType.APPLICATION_JSON_TYPE)
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .post(Entity.form(form), OAuthTokenResponse.class))
            .build();

    }

}
