package net.trajano.ms.example.authn;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.common.beans.JwksProvider;
import net.trajano.ms.common.oauth.GrantHandler;
import net.trajano.ms.common.oauth.GrantTypes;
import net.trajano.ms.common.oauth.OAuthTokenResponse;

@Component
@Configuration
public class SimpleAuthenticationGrantHandler implements
    GrantHandler {

    private static final String BASIC = "Basic";

    @Value("${authorizationEndpoint}")
    private URI authorizationEndpoint;

    @Value("${issuer}")
    private URI issuer;

    @Autowired
    private JwksProvider jwksProvider;

    /**
     * The password required to be passed into the authorization
     */
    @Value("${passwordRequired}")
    private String passwordRequired;

    @Override
    public String getGrantTypeHandled() {

        return GrantTypes.PASSWORD;
    }

    @Override
    public OAuthTokenResponse handler(final Client jaxRsClient,
        final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        final String username = form.getFirst("username");
        final String password = form.getFirst("password");

        if (!password.equals(passwordRequired)) {
            throw OAuthTokenResponse.unauthorized("invalid_grant", "Invalid username/password", BASIC);
        }
        try {
            final JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .audience(authorizationEndpoint.toASCIIString())
                .subject(username)
                .issuer(issuer.toASCIIString())
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plus(60, ChronoUnit.SECONDS)))
                .build();
            final String jwt = jwksProvider.sign(claims).serialize();

            final Form authorizationForm = new Form();
            authorizationForm.param("grant_type", GrantTypes.JWT_ASSERTION);
            authorizationForm.param("assertion", jwt);

            return jaxRsClient.target(authorizationEndpoint).request()
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION))
                .post(Entity.form(authorizationForm), OAuthTokenResponse.class);
        } catch (final JOSEException e) {
            throw new InternalServerErrorException(e);
        }
    }

}
