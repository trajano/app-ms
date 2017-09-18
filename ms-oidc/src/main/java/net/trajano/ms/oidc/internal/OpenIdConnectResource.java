package net.trajano.ms.oidc.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;

import io.swagger.annotations.Api;
import net.trajano.ms.common.JwksProvider;
import net.trajano.ms.common.TokenGenerator;
import net.trajano.ms.oidc.OpenIdConfiguration;

@Api
@Component
@Path("/oidc")
public class OpenIdConnectResource {

    @Autowired
    private ClientBuilder cb;

    @Autowired
    private JwksProvider jwksProvider;

    @Autowired
    @Qualifier("nonce")
    private Cache nonceCache;

    @Autowired
    private ServiceConfiguration serviceConfiguration;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Path("/auth")
    @GET
    public Response auth(@QueryParam("state") final String state,
        @QueryParam("issuer_id") final String issuerId,
        @Context final org.wso2.msf4j.Request req) {

        return Response.ok().status(Status.TEMPORARY_REDIRECT).header("Location", authUri(state, issuerId, req)).build();
    }

    @Path("/auth-uri")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public URI authUri(@QueryParam("state") final String state,
        @QueryParam("issuer_id") final String issuerId,
        @Context final org.wso2.msf4j.Request req) {

        if (issuerId == null) {
            throw new BadRequestException("Missing issuer_id");
        }

        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            throw new BadRequestException("Invalid issuer_id");
        }
        final URI redirectUri = UriBuilder.fromUri(serviceConfiguration.getRedirectUri()).path(issuerId).build();
        final URI buildAuthenticationRequestUri = issuerConfig.buildAuthenticationRequestUri(redirectUri, state, generateNonce(issuerId));
        return buildAuthenticationRequestUri;
    }

    @Path("/cb/{issuer_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response callback(@QueryParam("code") final String code,
        @PathParam("issuer_id") final String issuerId) throws MalformedURLException,
        IOException,
        ParseException,
        JOSEException {

        if (issuerId == null) {
            throw new BadRequestException("Missing issuer_id");
        }
        final IssuerConfig issuerConfig = serviceConfiguration.getIssuerConfig(issuerId);
        if (issuerConfig == null) {
            return Response.ok("Invalid issuer_id").status(Status.BAD_REQUEST).build();
        }
        final Client client = cb.build();
        final URI redirectUri = UriBuilder.fromUri(serviceConfiguration.getRedirectUri()).path(issuerId).build();
        final Form form = new Form();
        form.param("redirect_uri", redirectUri.toASCIIString());
        form.param("grant_type", "authorization_code");
        form.param("code", code);
        final OpenIdConfiguration openIdConfiguration = issuerConfig.getOpenIdConfiguration();
        final OpenIdToken openIdToken = client.target(openIdConfiguration.getTokenEndpoint())
            .request(MediaType.APPLICATION_JSON)
            .header("Authorization", issuerConfig.buildAuthorization())
            .buildPost(Entity.form(form)).invoke(OpenIdToken.class);

        // TODO cache this
        final JWKSet jwks = JWKSet.load(openIdConfiguration.getJwksUri().toURL());

        JOSEObject joseObject = JOSEObject.parse(openIdToken.getIdToken());
        if (joseObject instanceof JWEObject) {
            final JWEObject jwe = (JWEObject) joseObject;
            jwe.decrypt(new RSADecrypter(jwksProvider.getDecryptionKey(
                jwe.getHeader().getKeyID())));
            joseObject = JOSEObject.parse(jwe.getPayload().toString());
        }

        if (joseObject instanceof JWSObject) {
            final JWSObject jws = (JWSObject) joseObject;

            final JWSVerifier verifier = new RSASSAVerifier(((RSAKey) jwks.getKeyByKeyId(jws.getHeader().getKeyID())).toRSAPublicKey());
            if (!jws.verify(verifier)) {
                throw new NotAuthorizedException("verification failed", "JWT");
            }
        }

        final JWTClaimsSet claims = JWTClaimsSet.parse(joseObject.getPayload().toString());
        if (!claims.getAudience().contains(issuerConfig.getClientId())) {
            throw new InternalServerErrorException("client_id mismatch from IP");
        }
        if (!claims.getIssuer().equals(openIdConfiguration.getIssuer())) {
            throw new InternalServerErrorException("issuer mismatch from IP");
        }
        final String nonce = claims.getStringClaim("nonce");
        if (!issuerId.equals(nonceCache.get(nonce, String.class))) {
            throw new InternalServerErrorException("invalid nonce");
        }
        nonceCache.evict(nonce);
        System.out.println(joseObject.getPayload().toString());

        // TODO use claims.getSubject() and others to create a new OAuth token
        return Response.ok(claims.getSubject())
            .build();
    }

    private String generateNonce(final String issuerId) {

        final String nonce = tokenGenerator.newToken();
        nonceCache.putIfAbsent(nonce, issuerId);
        return nonce;
    }

}
