package net.trajano.ms.auth.internal;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * This will extract the username and password components of a basic
     * authorization header. If there is any problem this will return null.
     *
     * @param authorization
     *            authorization
     * @return an array consisting of the user name and password as strings.
     */
    public static String[] parseBasicAuthorization(String authorization) {

        if (authorization == null) {
            return null;
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
            return null;
        }
    }

    private HttpAuthorizationHeaders() {

    }
}
