package net.trajano.ms.gateway;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthTokenResponse implements
    Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -6845634801856757737L;

    @XmlElement(name = "access_token")
    private String accessToken;

    @XmlElement(name = "error")
    private String error;

    @XmlElement(name = "error_description")
    private String errorDescription;

    @XmlElement(name = "expires_in")
    private int expiresIn;

    @XmlElement(name = "refresh_token")
    private String refreshToken;

    @XmlElement(name = "token_type",
        required = true)
    private String tokenType;

    public String getAccessToken() {

        return accessToken;
    }

    public String getError() {

        return error;
    }

    public String getErrorDescription() {

        return errorDescription;
    }

    public int getExpiresIn() {

        return expiresIn;
    }

    public String getRefreshToken() {

        return refreshToken;
    }

    public String getTokenType() {

        return tokenType;
    }

    public boolean isError() {

        return error != null;
    }

    public void setAccessToken(final String accessToken) {

        this.accessToken = accessToken;
    }

    public void setError(final String error) {

        this.error = error;
    }

    public void setErrorDescription(final String errorDescription) {

        this.errorDescription = errorDescription;
    }

    public void setExpiresIn(final int expiresIn) {

        this.expiresIn = expiresIn;
    }

    public void setRefreshToken(final String refreshToken) {

        this.refreshToken = refreshToken;
    }

    public void setTokenType(final String tokenType) {

        this.tokenType = tokenType;
    }

}
