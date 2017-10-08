package net.trajano.ms.gateway;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdTokenResponse extends OAuthTokenResponse {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -1256763182860411545L;

    @XmlElement(name = "id_token",
        required = true)
    private String idToken;

    public String getIdToken() {

        return idToken;
    }

    public void setIdToken(final String idToken) {

        this.idToken = idToken;
    }
}
