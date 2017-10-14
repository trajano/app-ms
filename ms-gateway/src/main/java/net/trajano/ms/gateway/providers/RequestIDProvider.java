package net.trajano.ms.gateway.providers;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

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
     * Provides a new string useful for keys and access tokens.
     *
     * @return random string.
     */
    public String newRequestID() {

        final Random random = ThreadLocalRandom.current();
        final char[] buf = new char[LENGTH];
        for (int i = 0; i < LENGTH; ++i) {
            buf[i] = ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)];
        }
        return new String(buf);
    }

}
