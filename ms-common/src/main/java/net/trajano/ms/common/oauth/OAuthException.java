package net.trajano.ms.common.oauth;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class OAuthException extends WebApplicationException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    public OAuthException(final String error,
        final String errorDescription) {

        this(error, errorDescription, Status.BAD_REQUEST);

    }

    public OAuthException(final String error,
        final String errorDescription,
        final Status status) {

        super(Response.ok(new OAuthTokenError(error, errorDescription)).status(status).build());

    }
}
