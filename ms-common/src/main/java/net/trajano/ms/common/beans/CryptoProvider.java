package net.trajano.ms.common.beans;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This provides Java Cryptography objects. This is the only place where
 * {@link NoSuchAlgorithmException} will be thrown.
 *
 * @author Archimedes Trajano
 */
@Configuration
public class CryptoProvider {

    @Bean
    public KeyPairGenerator keyPairGenerator() throws NoSuchAlgorithmException {

        final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen;

    }

    @Bean
    public SecureRandom secureRandom() throws NoSuchAlgorithmException {

        return SecureRandom.getInstanceStrong();
    }
}
