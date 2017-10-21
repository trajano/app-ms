package net.trajano.ms.auth.token;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.trajano.ms.common.oauth.OAuthTokenResponse;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdTokenResponse extends OAuthTokenResponse {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -1256763182860411545L;

    @XmlElement(name = "id_token",
        required = true)
    private String idToken;

    public IdTokenResponse(String accessToken,
        String jwt,
        Integer expiresInSeconds) {
        this.setAccessToken(accessToken);
        this.setExpiresIn(expiresInSeconds);
        this.idToken = jwt;

    }

    public IdTokenResponse() {

    }

    public String getIdToken() {

        return idToken;
    }

    public void setIdToken(final String idToken) {

        this.idToken = idToken;
    }
}
