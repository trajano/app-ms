package net.trajano.ms.common.beans;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is a utility module that will be used to generate access tokens. It also
 * exposes the SecureRandom used so it can be utilized in other places to reduce
 * the cost of initialization.
 *
 * @author Archimedes Trajano
 */
@Component
public class TokenGenerator {

    /**
     * Only allow letters and numbers, no symbols. It makes it easier to copy and
     * paste for testing.
     */
    private static final char[] ALLOWED_TOKEN_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private static final int TOKEN_LENGTH = 32;

    private Random random;

    /**
     * Provides a new string useful for keys and access tokens.
     *
     * @return random string.
     */
    public String newToken() {

        final char[] buf = new char[TOKEN_LENGTH];
        for (int i = 0; i < TOKEN_LENGTH; ++i) {
            buf[i] = ALLOWED_TOKEN_CHARACTERS[random.nextInt(ALLOWED_TOKEN_CHARACTERS.length)];
        }
        return new String(buf);
    }

    @Autowired
    public void setRandom(final Random random) {

        this.random = random;
    }
}
