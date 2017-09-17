package net.trajano.ms.oidc;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IssuerConfig {

    @XmlElement(name = "client_id")
    private String clientId;

    @XmlElement(name = "client_secret")
    private String clientSecret;

    private String id;

    private URI uri;

    public String getClientId() {

        return clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public String getId() {

        return id;
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

    public void setUri(final URI uri) {

        this.uri = uri;
    }
}
