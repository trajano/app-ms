package net.trajano.ms.gateway.providers;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Configuration
@Component
public class AuthorizationHandlerProvider {

    @Value("${authorizationEndpoint}")
    private URI authorizationEndpoint;

    public Handler<RoutingContext> authorizationHandler() {

        // Checks if there is a bearer token, if not return a 401
        //
        return null;
    }
}
