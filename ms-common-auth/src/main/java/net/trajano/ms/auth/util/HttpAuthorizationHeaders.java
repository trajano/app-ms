package net.trajano.ms.auth.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.trajano.ms.core.ErrorResponses;

/**
 * Utility class to process HTTP Headers dealing with authorization.
 */
public final class HttpAuthorizationHeaders {

    /**
     * Basic Authorization pattern. It is "Basic " followed by a valid base64
     * string. The Base64 pattern was from
     * https://stackoverflow.com/a/8106083/242042
     */
    private static final Pattern BASIC_AUTHORIZATION_PATTERN = Pattern.compile("^Basic ((?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?)$");

    private static final Pattern BEARER_AUTHORIZATION_PATTERN = Pattern.compile("^Bearer ([-_A-Za-z0-9]+)$");

    public static String buildBasicAuthorization(final String username,
        final String password) {

        final StringBuilder b = new StringBuilder("Basic ");
        b.append(Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));
        return b.toString();
    }

    public static AuthorizationType getAuthorizationType(final String authorization) {

        if (authorization == null) {
            throw ErrorResponses.missingAuthorization();
        }
        if (authorization.startsWith("Bearer ")) {
            return AuthorizationType.BEARER;
        } else if (authorization.startsWith("Basic ")) {
            return AuthorizationType.BASIC;
        } else {
            throw ErrorResponses.invalidAuthorization();
        }
    }

    /**
     * This will extract the username and password components of a basic
     * authorization header.
     *
     * @param authorization
     *            authorization
     * @return an array consisting of the user name and password as strings.
     */
    public static String[] parseBasicAuthorization(final String authorization) {

        if (authorization == null) {
            throw ErrorResponses.missingAuthorization();
        }
        final Matcher m = BASIC_AUTHORIZATION_PATTERN.matcher(authorization);
        if (m.matches()) {
            final String decoded = new String(Base64.getDecoder().decode(m.group(1)), StandardCharsets.UTF_8);
            final int colonPosition = decoded.indexOf(':');
            return new String[] {
                decoded.substring(0, colonPosition),
                decoded.substring(colonPosition + 1)
            };
        } else {
            throw ErrorResponses.invalidRequest("authorization is not valid");
        }
    }

    public static String parseBeaerAuthorization(final String authorization) {

        if (authorization == null) {
            throw ErrorResponses.missingAuthorization();
        }
        final Matcher m = BEARER_AUTHORIZATION_PATTERN.matcher(authorization);
        if (m.matches()) {
            return m.group(1);
        } else {
            throw ErrorResponses.invalidRequest("authorization is not valid");
        }
    }

    private HttpAuthorizationHeaders() {

    }
}
