package net.trajano.ms.engine.jaxrs;

public interface PathsProvider {

    /**
     * An iterable collection of classes that have been annotated with Path to
     * indicate that it is used for routing.
     * 
     * @return path annotated classes
     */
    Iterable<Class<?>> getPathAnnotatedClasses();

}
