package net.trajano.ms.common;

import java.util.function.Predicate;

import javax.ws.rs.container.ResourceInfo;

/**
 * The function returns true if assertion is required for the URI provided.
 *
 * @author Archimedes Trajano
 */
public interface JwtAssertionRequiredFunction extends
    Predicate<ResourceInfo> {
}
