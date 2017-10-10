package net.trajano.ms.common.oauth;

import java.io.Serializable;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.spi.InternalServerErrorException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class OAuthTokenResponse implements
    Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -6845634801856757737L;

    public static BadRequestException badRequest(final String error,
        final String errorDescription) {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError(error);
        r.setErrorDescription(errorDescription);
        return new BadRequestException(Response
            .ok(r, MediaType.APPLICATION_JSON)
            .status(Status.BAD_REQUEST).build());
    }

    public static InternalServerErrorException internalServerError(final String message) {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError("server_error");
        r.setErrorDescription(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
        return new InternalServerErrorException(message, Response
            .ok(r, MediaType.APPLICATION_JSON)
            .status(Status.INTERNAL_SERVER_ERROR).build());
    }

    public static InternalServerErrorException internalServerError(final Throwable e) {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError("server_error");
        r.setErrorDescription(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
        return new InternalServerErrorException(e, Response
            .ok(r, MediaType.APPLICATION_JSON)
            .status(Status.INTERNAL_SERVER_ERROR).build());
    }

    public static NotAuthorizedException unauthorized(final String error,
        final String errorDescription,
        final String challenge) {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError(error);
        r.setErrorDescription(errorDescription);
        return new NotAuthorizedException(Response
            .ok(r, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.WWW_AUTHENTICATE, challenge)
            .status(Status.UNAUTHORIZED).build());
    }

    @XmlElement(name = "access_token")
    private String accessToken;

    @XmlElement(name = "error")
    private String error;

    @XmlElement(name = "error_description")
    private String errorDescription;

    @XmlElement(name = "expires_in")
    private Integer expiresIn;

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

    public boolean isExpiring() {

        return expiresIn != null;
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
