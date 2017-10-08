package net.trajano.ms.example.authn;

import java.util.List;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.trajano.ms.common.JwtNotRequired;
import net.trajano.ms.common.oauth.AllowAnyClientValidator;
import net.trajano.ms.common.oauth.BaseTokenResource;
import net.trajano.ms.common.oauth.GrantHandler;

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

}
