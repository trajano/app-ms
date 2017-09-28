package net.trajano.ms.common;

import java.util.function.Function;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * The function returns true if assertion is required for the URI provided.
 *
 * @author Archimedes Trajano
 */
public interface JwtAssertionRequiredFunction extends
    Function<ContainerRequestContext, Boolean> {
}
