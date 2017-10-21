package net.trajano.ms.example.authn;

import java.net.URI;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;

import net.trajano.ms.auth.token.ErrorCodes;
import net.trajano.ms.common.oauth.OAuthTokenResponse;
import net.trajano.ms.core.CryptoOps;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.BasicAuthDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import net.trajano.ms.common.oauth.AllowAnyClientValidator;
import net.trajano.ms.common.oauth.BaseTokenResource;
import net.trajano.ms.common.oauth.GrantHandler;
import net.trajano.ms.auth.token.GrantTypes;

/**
 * This works like the FORM based login of Java EE. It allows any user name as
 * long as the password is "password"
 */
@SwaggerDefinition(
    info = @Info(
        title = "Sample Authn Microservice",
        version = "1.0"))
@Component
@Path("/authn")
@PermitAll
public class AuthnResource  {

    @Context
    private Client client;

    @Autowired
    private CryptoOps cryptoOps;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public OAuthTokenResponse json(@FormParam("j_username") String username,
        @FormParam("j_password") String password,
        @FormParam("client_id") String clientId) {

        if (!"password".equals(password)) {
            throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "invalid username/password combination", "FORM");
        }
        JwtClaims claims = new JwtClaims();
        claims.setSubject(username);

        final Form form = new Form();
        form.param("grant_type", GrantTypes.JWT_ASSERTION);
        form.param("assertion", cryptoOps.sign(claims));
        form.param("client_id", clientId);

        return client.target(authorizationEndpoint).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.form(form), OAuthTokenResponse.class);

    }

    @Value("${authorizationEndpoint}")
    private URI authorizationEndpoint;

}
