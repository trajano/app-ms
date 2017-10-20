package net.trajano.ms.vertx.beans;

import java.util.function.Predicate;

import javax.ws.rs.container.ResourceInfo;

/**
 * The function returns true if assertion is required for the URI provided.
 *
 * @author Archimedes Trajano
 */
public interface JwtAssertionRequiredPredicate extends
    Predicate<ResourceInfo> {
}
