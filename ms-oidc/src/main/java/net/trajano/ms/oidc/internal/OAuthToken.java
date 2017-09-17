package net.trajano.ms.oidc.internal;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OAuthToken {

    @XmlElement(name = "access_token")
    private String accessToken;

    @XmlElement(name = "error")
    private String error;

    @XmlElement(name = "error_description")
    private String errorDescription;

    @XmlElement(name = "error_uri")
    private URI errorUri;

    /**
     * Number of seconds till expiration.
     */
    @XmlElement(name = "expires_in")
    private int expiresIn;

    @XmlElement(name = "refresh_token")
    private String refreshToken;

    @XmlElement(name = "state")
    private String state;

    @XmlElement(name = "token_type")
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

    public URI getErrorUri() {

        return errorUri;
    }

    public int getExpiresIn() {

        return expiresIn;
    }

    public String getRefreshToken() {

        return refreshToken;
    }

    public String getState() {

        return state;
    }

    public String getTokenType() {

        return tokenType;
    }

    /**
     * Checks if this is an error message.
     *
     * @return <code>true</code> if this an error OAuth Token.
     */
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

    public void setErrorUri(final URI errorUri) {

        this.errorUri = errorUri;
    }

    public void setExpiresIn(final int expiresIn) {

        this.expiresIn = expiresIn;
    }

    public void setRefreshToken(final String refreshToken) {

        this.refreshToken = refreshToken;
    }

    public void setState(final String state) {

        this.state = state;
    }

    public void setTokenType(final String tokenType) {

        this.tokenType = tokenType;
    }

}
