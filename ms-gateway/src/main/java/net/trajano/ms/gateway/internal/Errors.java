package net.trajano.ms.gateway.internal;

import io.vertx.core.json.JsonObject;

public final class Errors {

    public static JsonObject build(final String error,
        final String errorDescription) {

        return new JsonObject()
            .put("error", error)
            .put("error_description", errorDescription);
    }

    public static JsonObject invalidGrant(final String errorDescription) {

        return build("invalid_grant", errorDescription);
    }

    public static JsonObject invalidRequest(final String errorDescription) {

        return build("invalid_request", errorDescription);
    }

    public static JsonObject serverError(final String errorDescription) {

        return build("server_error", errorDescription);
    }

    public static JsonObject unauthorizedClient(final String errorDescription) {

        return build("unauthorized_client", errorDescription);

    }

    private Errors() {

    }
}
