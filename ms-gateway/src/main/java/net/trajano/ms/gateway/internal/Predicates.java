package net.trajano.ms.gateway.internal;

import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;
import static io.vertx.core.http.HttpHeaders.DATE;
import static net.trajano.ms.gateway.providers.RequestIDProvider.REQUEST_ID;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Predicates {

    /**
     * A header is can be forwarded as long as it is not restricted.
     * {@link io.vertx.core.http.HttpHeaders#AUTHORIZATION} is restricted unless it
     * is "Basic" which implies that it is a client authorization that can be passed
     * down.
     */
    public static final Predicate<Map.Entry<String, String>> HEADER_FORWARDABLE;

    /**
     * Set of restricted headers.
     */
    private static final Set<String> RESTRICTED_HEADERS;

    /**
     * URI that points to the JWKS.
     */
    private static final String X_JWKS_URI = "X-JWKS-URI";

    private static final String X_JWT_ASSERTION = "X-JWT-Assertion";

    private static final String X_JWT_AUDIENCE = "X-JWT-Audience";

    static {
        RESTRICTED_HEADERS = Stream.of(X_JWKS_URI, X_JWT_ASSERTION, X_JWT_AUDIENCE, REQUEST_ID, DATE, AUTHORIZATION).map(CharSequence::toString).collect(Collectors.toSet());
        HEADER_FORWARDABLE = e -> e.getKey().contentEquals(AUTHORIZATION) && e.getValue().startsWith("Basic ") || !RESTRICTED_HEADERS.contains(e.getKey());
    }

    /**
     * Prevent instantiation of utility class.
     */
    private Predicates() {

    }
}
