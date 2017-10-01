package net.trajano.ms.common;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Flags a resource as JWT not required.
 *
 * @author Archimedes Trajano
 */
@Retention(RUNTIME)
@Target({
    METHOD,
    TYPE
})
public @interface JwtNotRequired {

}
