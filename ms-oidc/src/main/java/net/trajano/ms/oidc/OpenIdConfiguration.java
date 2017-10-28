package net.trajano.ms.oidc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.InternalServerErrorException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenIdConfiguration {

    @XmlElement(name = "authorization_endpoint")
    private URI authorizationEndpoint;

    @XmlTransient
    private HttpsJwks httpsJwks;

    @XmlElement(name = "issuer")
    private String issuer;

    @XmlElement(name = "jwks_uri")
    private URI jwksUri;

    @XmlElement(name = "response_types_supported",
        type = String.class)
    private List<String> responseTypesSupported;

    @XmlElement(name = "revocation_endpoint")
    private URI revocationEndpoint;

    @XmlElement(name = "token_endpoint")
    private URI tokenEndpoint;

    @XmlElement(name = "userinfo_endpoint")
    private URI userinfoEndpoint;

    public URI getAuthorizationEndpoint() {

        return authorizationEndpoint;
    }

    public HttpsJwks getHttpsJwks() {

        if (httpsJwks == null) {
            httpsJwks = new HttpsJwks(jwksUri.toASCIIString());
        }
        return httpsJwks;
    }

    public String getIssuer() {

        return issuer;
    }

    public JsonWebKeySet getJwks() {

        try {
            return new JsonWebKeySet(getHttpsJwks().getJsonWebKeys());
        } catch (final JoseException e) {
            throw new InternalServerErrorException(e);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    public URI getJwksUri() {

        return jwksUri;
    }

    public List<String> getResponseTypesSupported() {

        return responseTypesSupported;
    }

    public URI getRevocationEndpoint() {

        return revocationEndpoint;
    }

    public URI getTokenEndpoint() {

        return tokenEndpoint;
    }

    public URI getUserinfoEndpoint() {

        return userinfoEndpoint;
    }

    public void setAuthorizationEndpoint(final URI authorizationEndpoint) {

        this.authorizationEndpoint = authorizationEndpoint;
    }

    public void setIssuer(final String issuer) {

        this.issuer = issuer;
    }

    public void setJwksUri(final URI jwksUri) {

        this.jwksUri = jwksUri;
    }

    public void setResponseTypesSupported(final List<String> responseTypesSupported) {

        this.responseTypesSupported = responseTypesSupported;
    }

    public void setRevocationEndpoint(final URI revocationEndpoint) {

        this.revocationEndpoint = revocationEndpoint;
    }

    public void setTokenEndpoint(final URI tokenEndpoint) {

        this.tokenEndpoint = tokenEndpoint;
    }

    public void setUserinfoEndpoint(final URI userinfoEndpoint) {

        this.userinfoEndpoint = userinfoEndpoint;
    }

    /*
     * "subject_types_supported": [ "public" ],
     * "id_token_signing_alg_values_supported": [ "RS256" ], "scopes_supported": [
     * "openid", "email", "profile" ], "token_endpoint_auth_methods_supported": [
     * "client_secret_post", "client_secret_basic" ], "claims_supported": [ "aud",
     * "email", "email_verified", "exp", "family_name", "given_name", "iat", "iss",
     * "locale", "name", "picture", "sub" ], "code_challenge_methods_supported": [
     * "plain", "S256" ] }
     */

}
