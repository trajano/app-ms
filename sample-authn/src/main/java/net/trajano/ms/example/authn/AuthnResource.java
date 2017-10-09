package net.trajano.ms.example.authn;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.trajano.ms.common.JwtNotRequired;
import net.trajano.ms.common.oauth.AllowAnyClientValidator;
import net.trajano.ms.common.oauth.BaseTokenResource;
import net.trajano.ms.common.oauth.GrantHandler;
import net.trajano.ms.common.oauth.GrantTypes;

@SwaggerDefinition(
    info = @Info(
        title = "Sample Authn Microservice",
        version = "1.0"))
@Api
@Component
@Path("/authn")
@JwtNotRequired
public class AuthnResource extends BaseTokenResource {

    @Autowired
    public AuthnResource(
        final List<GrantHandler> grantHandlers) {

        super(new AllowAnyClientValidator(), grantHandlers);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "grant_type",
            value = "Grant type",
            required = true,
            dataType = "java.lang.String",
            example = GrantTypes.CLIENT_CREDENTIALS,
            paramType = "form"),
        @ApiImplicitParam(name = "client_id",
            value = "Client ID",
            dataType = "java.lang.String",
            required = true,
            paramType = "form"),
        @ApiImplicitParam(name = "client_secret",
            value = "Client Secret",
            dataType = "java.lang.String",
            required = true,
            paramType = "form")
    })
    @ApiOperation(value = "token",
        authorizations = @Authorization("Basic"))
    @Override
    public Response token(final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        return super.token(httpHeaders, form);
    }

}
