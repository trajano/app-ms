package net.trajano.ms.oidc.internal;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.trajano.ms.oidc.OpenIdConfiguration;

@XmlRootElement
public class IssuerConfig {

    @XmlElement(name = "client_id")
    private String clientId;

    @XmlElement(name = "client_secret")
    private String clientSecret;

    private String id;

    @XmlTransient
    private OpenIdConfiguration openIdConfiguration;

    private URI uri;

    public URI buildAuthenticationRequestUri(final String state) {

        System.out.println(openIdConfiguration.getResponseTypesSupported());
        openIdConfiguration.getAuthorizationEndpoint();
        // TODO Auto-generated method stub
        //        UriBuilder.fromUri(openIdConfiguration.getAuthorizationEndpoint());
        return null;
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
