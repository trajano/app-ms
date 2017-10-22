package net.trajano.ms.auth.token;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdTokenResponse extends OAuthTokenResponse {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -1256763182860411545L;

    @XmlElement(name = "aud")
    private Collection<String> audience;

    @XmlElement(name = "id_token",
        required = true)
    private String idToken;

    public IdTokenResponse() {

    }

    public IdTokenResponse(final String accessToken,
        final String jwt,
        final Collection<String> audience,
        final Integer expiresInSeconds) {

        setAccessToken(accessToken);
        setExpiresIn(expiresInSeconds);
        idToken = jwt;
        this.audience = audience;

    }

    public String getIdToken() {

        return idToken;
    }

    public void setIdToken(final String idToken) {

        this.idToken = idToken;
    }
}
