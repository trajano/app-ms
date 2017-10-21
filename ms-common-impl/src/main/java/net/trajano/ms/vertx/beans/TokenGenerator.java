package net.trajano.ms.vertx.beans;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
     * Only allow letters and numbers, no symbols. It makes it easier to copy
     * and paste for testing.
     */
    private static final char[] ALLOWED_TOKEN_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /**
     * This makes the possible combinations higher than a 256-bit key.
     */
    private static final int TOKEN_LENGTH = 64;

    private Random random;

    /**
     * Initializes the random source.
     *
     * @throws NoSuchAlgorithmException
     *             this should not happen.
     */
    @PostConstruct
    public void initializeRandom() throws NoSuchAlgorithmException {

        random = SecureRandom.getInstanceStrong();
    }

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

}
