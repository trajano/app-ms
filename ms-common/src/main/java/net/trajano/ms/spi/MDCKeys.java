package net.trajano.ms.spi;

/**
 * MDC Keys. These are used by the core API. The service implementation should
 * populate the {@link org.slf4j.MDC} using the following keys
 */
public final class MDCKeys {

    /**
     * Host name and port.
     */
    public static final String HOST = "Host";

    /**
     * JWT ID. Represents a client session;
     */
    public static final String JWT_ID = "X-JWT-ID";

    /**
     * Request ID. Represents a single request coming from the client.
     */
    public static final String REQUEST_ID = "X-Request-ID";

    /**
     * Request method. Represents the method URI being requested upon.
     */
    public static final String REQUEST_METHOD = "X-Request-Method";

    /**
     * Request URI. Represents the URI being requested.
     */
    public static final String REQUEST_URI = "X-Request-URI";

    /**
     * Prevent instantiation of constants class.
     */
    private MDCKeys() {

    }

}
