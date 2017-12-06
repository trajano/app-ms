package net.trajano.ms.auth.jsonclientvalidator;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Client information.
 *
 * @author Archimedes Trajano
 */
@XmlRootElement
public class ClientInfo {

    @XmlElement(name = "client_id",
        required = true)
    private String clientId;

    @XmlElement(name = "client_secret",
        required = true)
    private String clientSecret;

    @XmlElement(name = "grant_types",
        required = true,
        type = String.class)
    private Set<String> grantTypes = new HashSet<>();

    @XmlElement(name = "jwks_uri",
        required = false)
    private URI jwksUri;

    @XmlElement(name = "redirect_uri",
        required = false)
    private URI redirectUri;

    @XmlElement(name = "origin",
        required = false)
    private URI origin;

    public String getClientId() {

        return clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public Set<String> getGrantTypes() {

        return grantTypes;
    }

    public URI getJwksUri() {

        return jwksUri;
    }

    public boolean matches(final String grantType,
        final String clientId,
        final String clientSecret) {

        return grantTypes.contains(grantType) &&
            this.clientId.equals(clientId) &&
            this.clientSecret.equals(clientSecret);
    }

    /**
     * Matches with no grant type.
     * 
     * @param clientId
     *            client ID
     * @param clientSecret
     *            client secret
     * @return true if the ID and secret match the one provided by info.
     */
    public boolean matches(final String clientId,
        final String clientSecret) {

        return this.clientId.equals(clientId) &&
            this.clientSecret.equals(clientSecret);
    }

    public void setClientId(final String clientId) {

        this.clientId = clientId;
    }

    public void setClientSecret(final String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public void setGrantTypes(final Set<String> grantTypes) {

        this.grantTypes = grantTypes;
    }

    public void setJwksUri(final URI jwksUri) {

        this.jwksUri = jwksUri;
    }

    public URI getRedirectUri() {

        return redirectUri;
    }

    public void setRedirectUri(URI redirectUri) {

        this.redirectUri = redirectUri;
    }

    public boolean isOriginAllowed(URI origin) {

        return this.origin.equals(origin);
    }

    public URI getOrigin() {

        return origin;
    }

    public void setOrigin(URI origin) {

        this.origin = origin;
    }
}
