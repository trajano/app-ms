package net.trajano.ms.common;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jose4j.jwk.JsonWebKey.OutputControlLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This endpoint is exposed by every microservice to provide JWKS that is used
 * by the microservice.
 *
 * @author TrajanAr
 */
@Component
@Path("jwks")
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicKeySet() {

        return Response.ok(jwksProvider.getKeySet().toJson(OutputControlLevel.PUBLIC_ONLY)).build();
    }
}
