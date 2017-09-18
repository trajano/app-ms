package net.trajano.ms.oidc;

import java.net.URI;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenIdConfiguration {

    @XmlElement(name = "authorization_endpoint")
    private URI authorizationEndpoint;

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

    public String getIssuer() {

        return issuer;
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
     * "token_endpoint": "https://www.googleapis.com/oauth2/v4/token",
     * "userinfo_endpoint": "https://www.googleapis.com/oauth2/v3/userinfo",
     * "revocation_endpoint": "https://accounts.google.com/o/oauth2/revoke",
     * "jwks_uri": "https://www.googleapis.com/oauth2/v3/certs",
     * "response_types_supported": [ "code", "token", "id_token", "code token",
     * "code id_token", "token id_token", "code token id_token", "none" ],
     * "subject_types_supported": [ "public" ],
     * "id_token_signing_alg_values_supported": [ "RS256" ], "scopes_supported": [
     * "openid", "email", "profile" ], "token_endpoint_auth_methods_supported": [
     * "client_secret_post", "client_secret_basic" ], "claims_supported": [ "aud",
     * "email", "email_verified", "exp", "family_name", "given_name", "iat", "iss",
     * "locale", "name", "picture", "sub" ], "code_challenge_methods_supported": [
     * "plain", "S256" ] }
     */

}
