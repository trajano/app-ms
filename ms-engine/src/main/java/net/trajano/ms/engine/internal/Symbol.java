package net.trajano.ms.engine.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * This provides a "Symbol" instance of an object which can be used as markers.
 * It provides only two handlers that is equals and hashCode
 *
 * @author Archimedes Trajano
 */
public final class Symbol {

    private static class SymbolInvocationHandler implements
        InvocationHandler,
        MethodHandler {

        private final String symbolName;

        public SymbolInvocationHandler(final String symbolName) {

            this.symbolName = symbolName;
        }

        @Override
        public Object invoke(final Object proxy,
            final Method method,
            final Method proceed,
            final Object[] args) throws Throwable {

            return invoke(proxy, method, args);
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

    public static <T> T newSymbol(final Class<T> clazz) {

        return newSymbol(clazz, clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> T newSymbol(final Class<T> clazz,
        final String name) {

        try {
            if (clazz.isInterface()) {
                return (T) Proxy.newProxyInstance(Symbol.class.getClassLoader(), new Class<?>[] {
                    clazz
                }, new SymbolInvocationHandler(name));
            } else {
                final ProxyFactory pf = new ProxyFactory();
                pf.setSuperclass(clazz);
                final T proxy = (T) pf.createClass().newInstance();
                ((javassist.util.proxy.Proxy) proxy).setHandler(new SymbolInvocationHandler(name));
                return proxy;

            }
        } catch (final IllegalArgumentException
            | InstantiationException
            | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
