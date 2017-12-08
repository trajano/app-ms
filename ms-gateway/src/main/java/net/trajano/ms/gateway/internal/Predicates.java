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

    public static final Predicate<Map.Entry<String, String>> HEADER_FORWARDABLE;

    private static final Set<String> RESTRICTED_HEADERS;

    private static final String X_JWKS_URI = "X-JWKS-URI";

    private static final String X_JWT_ASSERTION = "X-JWT-Assertion";

    private static final String X_JWT_AUDIENCE = "X-JWT-Audience";

    static {
        RESTRICTED_HEADERS = Stream.of(X_JWKS_URI, X_JWT_ASSERTION, X_JWT_AUDIENCE, REQUEST_ID, AUTHORIZATION, DATE).map(CharSequence::toString).collect(Collectors.toSet());
        HEADER_FORWARDABLE = e -> !RESTRICTED_HEADERS.contains(e.getKey());
    }

    private Predicates() {

    }
}
