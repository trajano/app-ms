package net.trajano.ms.oidc.internal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OpenIdToken extends OAuthToken {

    @XmlElement(name = "id_token")
    private String idToken;

    public String getIdToken() {

        return idToken;
    }

    public void setIdToken(final String idToken) {

        this.idToken = idToken;
    }
}
