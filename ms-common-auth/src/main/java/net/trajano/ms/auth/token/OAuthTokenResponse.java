package net.trajano.ms.auth.token;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@ApiModel(description = "OAuth 2.0 Token Response")
public class OAuthTokenResponse implements
    Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -6845634801856757737L;

    @ApiModelProperty(name = "access_token",
        value = "The access token issued by the authorization server.",
        required = true)
    @XmlElement(name = "access_token",
        required = true)
    private String accessToken;

    @ApiModelProperty(name = "expires_in",
        value = "The lifetime in seconds of the access token.  For example, the value \"3600\" denotes that the access token will expire in one hour from the time the response was generated.",
        allowableValues = "[1,infinity]",
        example = "3600")
    @XmlElement(name = "expires_in")
    private Integer expiresIn;

    @ApiModelProperty(name = "refresh_token",
        value = "The refresh token, which can be used to obtain new access tokens using the same authorization grant")
    @XmlElement(name = "refresh_token")
    private String refreshToken;

    @ApiModelProperty(name = "token_type",
        value = "The type of the token issued",
        example = "Bearer",
        required = true)
    @XmlElement(name = "token_type",
        required = true)
    private String tokenType;

    public String getAccessToken() {

        return accessToken;
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

    public boolean isExpiring() {

        return expiresIn != null;
    }

    public void setAccessToken(final String accessToken) {

        this.accessToken = accessToken;
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
