package net.trajano.ms.engine.internal.jerseyspring;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;

/**
 * HK2 injection resolver for Spring framework {@link Autowired} annotation
 * injection.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 * @author Vetle Leinonen-Roeim (vetle at roeim.net)
 */
@Singleton
public class AutowiredInjectResolver implements
    InjectionResolver<Autowired> {

    private static final Logger LOGGER = Logger.getLogger(AutowiredInjectResolver.class.getName());

    private volatile ApplicationContext ctx;

    /**
     * Create a new instance.
     *
     * @param ctx
     *            Spring application context.
     */
    public AutowiredInjectResolver(final ApplicationContext ctx) {

        this.ctx = ctx;
    }

    private DependencyDescriptor createSpringDependencyDescriptor(final Injectee injectee) {

        final AnnotatedElement annotatedElement = injectee.getParent();

        if (annotatedElement.getClass().isAssignableFrom(Field.class)) {
            return new DependencyDescriptor((Field) annotatedElement, !injectee.isOptional());
        } else if (annotatedElement.getClass().isAssignableFrom(Method.class)) {
            return new DependencyDescriptor(
                new MethodParameter((Method) annotatedElement, injectee.getPosition()), !injectee.isOptional());
        } else {
            return new DependencyDescriptor(
                new MethodParameter((Constructor<?>) annotatedElement, injectee.getPosition()), !injectee.isOptional());
        }
    }

    @Override
    public Class<Autowired> getAnnotation() {

        return Autowired.class;
    }

    private Object getBeanFromSpringContext(final String beanName,
        final Injectee injectee,
        final boolean required) {

        try {
            final DependencyDescriptor dependencyDescriptor = createSpringDependencyDescriptor(injectee);
            final Set<String> autowiredBeanNames = new HashSet<>(1);
            autowiredBeanNames.add(beanName);
            return ctx.getAutowireCapableBeanFactory().resolveDependency(dependencyDescriptor, null,
                autowiredBeanNames, null);
        } catch (final NoSuchBeanDefinitionException e) {
            if (required) {
                LOGGER.warning(e.getMessage());
                throw e;
            }
            return null;
        }
    }

    @Override
    public boolean isConstructorParameterIndicator() {

        return false;
    }

    @Override
    public boolean isMethodParameterIndicator() {

        return false;
    }

    @Override
    public Object resolve(final Injectee injectee) {

        final AnnotatedElement parent = injectee.getParent();
        String beanName = null;
        if (parent != null) {
            final Qualifier an = parent.getAnnotation(Qualifier.class);
            if (an != null) {
                beanName = an.value();
            }
        }
        final boolean required = parent != null ? parent.getAnnotation(Autowired.class).required() : false;
        return getBeanFromSpringContext(beanName, injectee, required);
    }
}
