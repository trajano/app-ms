package net.trajano.ms.engine.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This provides a "Symbol" instance of an object which can be used as markers.
 * It provides only two handlers that is equals and hashCode
 *
 * @author Archimedes Trajano
 */
public final class Symbol
    implements
    InvocationHandler {

    /**
     * A marker interface to indicate that an object is a symbol.
     */
    private static interface ISymbol {
    }

    /**
     * Checks whether a given object is a symbol.
     * 
     * @param o
     *            object to test
     * @return true if it is a symbol.
     */
    public static boolean isSymbol(final Object o) {

        return o instanceof ISymbol;
    }

    public static <T> T newSymbol(final Class<T> clazz) {

        return newSymbol(clazz, clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> T newSymbol(final Class<T> clazz,
        final String name) {

        try {
            return (T) Proxy.newProxyInstance(Symbol.class.getClassLoader(), new Class[] {
                clazz,
                ISymbol.class
            }, new Symbol(name));
        } catch (final IllegalArgumentException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final String symbolName;

    /**
     * Constructs Symbol.
     *
     * @param symbolName
     *            symbol name
     */
    private Symbol(final String symbolName) {

        this.symbolName = symbolName;
    }

    @Override
    public Object invoke(final Object proxy,
        final Method method,
        final Object[] args) throws Throwable {

        if ("equals".equals(method.getName())) {
            return proxy == args[0];
        } else if ("hashCode".equals(method.getName())) {
            return symbolName.hashCode();
        } else if ("toString".equals(method.getName())) {
            return symbolName;
        }
        throw new UnsupportedOperationException();
    }
}
