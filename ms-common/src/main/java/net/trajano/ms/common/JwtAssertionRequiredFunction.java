package net.trajano.ms.common;

import java.util.function.Function;

/**
 * The function returns true if assertion is required for the URI provided.
 *
 * @author Archimedes Trajano
 */
public interface JwtAssertionRequiredFunction extends
    Function<String, Boolean> {
}
