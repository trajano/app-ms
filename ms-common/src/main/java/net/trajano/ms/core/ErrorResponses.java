package net.trajano.ms.core;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This provides common error responses used by the system.
 */
public final class ErrorResponses {

    public static BadRequestException badRequest(final String error,
        final String errorDescription) {

        return new BadRequestException(Response
            .status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(error, errorDescription))
            .type(MediaType.APPLICATION_JSON)
            .build());
    }

    public static BadRequestException invalidRequest(final String errorDescription) {

        return new BadRequestException(Response
            .status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(ErrorCodes.INVALID_REQUEST, errorDescription))
            .type(MediaType.APPLICATION_JSON)
            .build());
    }

    public static InternalServerErrorException internalServerError(final String message) {

        return new InternalServerErrorException(Response
            .status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(ErrorCodes.SERVER_ERROR, message))
            .type(MediaType.APPLICATION_JSON)
            .build());

    }

    public static NotAuthorizedException unauthorized(final String error,
        final String errorDescription,
        final String challenge) {

        final ErrorResponse r = new ErrorResponse(error, errorDescription);
        return new NotAuthorizedException(Response
            .status(Response.Status.UNAUTHORIZED)
            .entity(r)
            .type(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.WWW_AUTHENTICATE, challenge)
            .build());
    }

    private ErrorResponses() {

    }
}
