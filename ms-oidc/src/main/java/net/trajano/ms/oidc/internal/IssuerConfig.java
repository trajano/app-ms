package net.trajano.ms.oidc.internal;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.trajano.ms.oidc.OpenIdConfiguration;

@XmlRootElement
public class IssuerConfig {

    @XmlElement(name = "client_id",
        required = true)
    private String clientId;

    @XmlElement(name = "client_secret",
        required = true)
    private String clientSecret;

    private String display;

    @XmlElement(required = true)
    private String id;

    @XmlTransient
    private OpenIdConfiguration openIdConfiguration;

    private String prompt;

    @XmlElement(name = "scope",
        required = true)
    private String scope;

    @XmlElement(required = true)
    private URI uri;

    public URI buildAuthenticationRequestUri(final URI redirectUri,
        final String state) {

        openIdConfiguration.getAuthorizationEndpoint();
        final UriBuilder b = UriBuilder.fromUri(openIdConfiguration.getAuthorizationEndpoint());
        b.queryParam("response_type", "code");
        b.queryParam("scope", scope);
        b.queryParam("client_id", clientId);
        b.queryParam("redirect_uri", redirectUri);
        if (state != null) {
            b.queryParam("state", state);
        }
        if (display != null) {
            b.queryParam("display", display);
        }
        if (prompt != null) {
            b.queryParam("prompt", prompt);
        }
        return b
            .build();
    }

    public String getClientId() {

        return clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public String getId() {

        return id;
    }

    public OpenIdConfiguration getOpenIdConfiguration() {

        return openIdConfiguration;
    }

    public URI getUri() {

        return uri;
    }

    public void setClientId(final String clientId) {

        this.clientId = clientId;
    }

    public void setClientSecret(final String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public void setOpenIdConfiguration(final OpenIdConfiguration openIdConfiguration) {

        this.openIdConfiguration = openIdConfiguration;
    }

    public void setUri(final URI uri) {

        this.uri = uri;
    }
}
