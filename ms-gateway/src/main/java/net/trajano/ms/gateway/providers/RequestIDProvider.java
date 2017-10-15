package net.trajano.ms.gateway.providers;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import io.vertx.ext.web.RoutingContext;

@Component
public class RequestIDProvider {

    /**
     * Only allow letters and numbers, no symbols. It makes it easier to copy
     * and paste for testing.
     */
    private static final char[] ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /**
     * Short length token just enough to be unique within a daily log file.
     */
    private static final int LENGTH = 8;

    /**
     * This is the header and map key used to represent the request ID.
     */
    public static final String REQUEST_ID = "X-Request-ID";

    /**
     * Provides a new request ID, adds it to the context along with the response
     * headers and sets the MDC as well.
     *
     * @param context
     *            routing context
     * @return random string.
     */
    public String newRequestID(final RoutingContext context) {

        final Random random = ThreadLocalRandom.current();
        final char[] buf = new char[LENGTH];
        for (int i = 0; i < LENGTH; ++i) {
            buf[i] = ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)];
        }
        final String requestID = new String(buf);
        MDC.put(REQUEST_ID, requestID);
        context.data().put(REQUEST_ID, requestID);
        context.response().putHeader(REQUEST_ID, requestID);
        return requestID;
    }

}
