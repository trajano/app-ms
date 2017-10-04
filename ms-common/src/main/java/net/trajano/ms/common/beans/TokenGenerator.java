package net.trajano.ms.common.beans;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.annotation.PostConstruct;

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

    private static final int TOKEN_LENGTH = 21;

    private SecureRandom random;

    /**
     * Initializes the random
     *
     * @throws NoSuchAlgorithmException
     */
    @PostConstruct
    public void init() throws NoSuchAlgorithmException {

        random = SecureRandom.getInstanceStrong();
    }

    /**
     * Provides a new string useful for keys and access tokens.
     *
     * @return random string.
     */
    public String newToken() {

        final byte[] buf = new byte[TOKEN_LENGTH];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().encodeToString(buf);
    }

    public SecureRandom random() {

        return random;
    }
}
