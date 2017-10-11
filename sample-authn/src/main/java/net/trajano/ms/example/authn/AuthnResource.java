package net.trajano.ms.example.authn;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.BasicAuthDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import net.trajano.ms.common.JwtNotRequired;
import net.trajano.ms.common.oauth.AllowAnyClientValidator;
import net.trajano.ms.common.oauth.BaseTokenResource;
import net.trajano.ms.common.oauth.GrantHandler;
import net.trajano.ms.common.oauth.GrantTypes;

@SwaggerDefinition(
    securityDefinition = @SecurityDefinition(basicAuthDefinitions = @BasicAuthDefinition(key = "client",
        description = "Client ID/Secret")),
    info = @Info(
        title = "Sample Authn Microservice",
        version = "1.0"))
@Api(tags = "unprotected")
@Component
@Path("/authn")
@JwtNotRequired
public class AuthnResource extends BaseTokenResource {

    @Autowired
    public AuthnResource(
        final List<GrantHandler> grantHandlers) {

        super(new AllowAnyClientValidator(), grantHandlers);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response json(@Context final HttpHeaders httpHeaders,
        final AuthnRequest req) {

        final MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
        form.putSingle("grant_type", req.getGrantType());
        form.putSingle("username", req.getUsername());
        form.putSingle("password", req.getPassword());
        return super.token(httpHeaders, form);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "grant_type",
            value = "Grant type",
            required = true,
            dataType = "java.lang.String",
            example = GrantTypes.PASSWORD,
            paramType = "form"),
        @ApiImplicitParam(name = "username",
            value = "Username",
            dataType = "java.lang.String",
            required = true,
            paramType = "form"),
        @ApiImplicitParam(name = "password",
            value = "Password",
            dataType = "java.lang.String",
            required = true,
            paramType = "form")
    })
    @Override
    public Response token(final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        return super.token(httpHeaders, form);
    }

}
