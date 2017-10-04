package net.trajano.ms.engine.internal.swagger;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.reflections.Reflections;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.config.Scanner;

/**
 * This
 *
 * @author Archimedes Trajano
 */
public class ApplicationScanner implements
    Scanner {

    private final Set<Class<?>> classes;

    private boolean shouldPrettyPrint;

    public ApplicationScanner(final Application application) {

        classes = new HashSet<>();
        final Set<Class<?>> resourceClasses = application.getClasses();
        if (resourceClasses.isEmpty()) {
            final String packageName = application.getClass().getPackage().getName();
            final Reflections reflections = new Reflections(packageName);
            reflections.getTypesAnnotatedWith(Api.class).forEach(clazz -> classes.add(clazz));
            reflections.getTypesAnnotatedWith(SwaggerDefinition.class).forEach(clazz -> classes.add(clazz));
        } else {
            classes.add(application.getClass());
            resourceClasses.forEach(clazz -> classes.add(clazz));
        }

    }

    public ApplicationScanner(final Class<? extends Application> applicationClass) throws InstantiationException,
        IllegalAccessException {

        this(applicationClass.newInstance());
    }

    @Override
    public Set<Class<?>> classes() {

        return classes;
    }

    @Override
    public boolean getPrettyPrint() {

        return shouldPrettyPrint;
    }

    @Override
    public void setPrettyPrint(final boolean shouldPrettyPrint) {

        this.shouldPrettyPrint = shouldPrettyPrint;

    }

}
