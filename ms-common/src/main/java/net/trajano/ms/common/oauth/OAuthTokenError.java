package net.trajano.ms.common.oauth;

public class OAuthTokenError extends OAuthTokenResponse {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -3772495171481082522L;

    public OAuthTokenError(final String error,
        final String errorDescription) {

        setError(error);
        setErrorDescription(errorDescription);
    }
}
