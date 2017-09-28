package net.trajano.ms.common.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.trajano.ms.common.JwksProvider;
import net.trajano.ms.common.JwtNotRequired;

/**
 * This endpoint is exposed by every microservice to provide JWKS that is used
 * by the microservice.
 *
 * @author Archimedes Trajano
 */
@Component
@Api
@Path("/jwks")
@JwtNotRequired
public class JwksResource {

    /**
     * JWKS provider
     */
    @Autowired
    private JwksProvider jwksProvider;

    /**
     * Only return the public keys.
     *
     * @return public key set.
     */
    @ApiOperation(value = "Get public keys",
        response = Response.class,
        notes = "Provides the JWKS of public keys used for JWS and JWE for clients.")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getPublicKeySet() {

        return jwksProvider.getKeySet().toPublicJWKSet().toString();
    }

    public void setJwksProvider(final JwksProvider jwksProvider) {

        this.jwksProvider = jwksProvider;
    }
}
