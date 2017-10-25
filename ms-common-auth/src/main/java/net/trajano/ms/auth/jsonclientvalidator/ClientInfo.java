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
}
