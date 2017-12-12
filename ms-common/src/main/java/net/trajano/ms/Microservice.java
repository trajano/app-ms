package net.trajano.ms;

import static java.lang.String.format;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.ws.rs.core.Application;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;

import net.trajano.ms.spi.MicroserviceEngine;

/**
 * Class used to bootstrap the Microservice engine. This takes a JAX-RS
 * {@link javax.ws.rs.core.Application} class as the application entry point.
 * Here is an example of how it is used.
 *
 * <pre>
 * import javax.ws.rs.core.Application;
 * import org.springframework.boot.autoconfigure.SpringBootApplication;
 * import net.trajano.ms.Microservice;
 *
 * &#64;SpringBootApplication
 * public class SampleMS extends Application {
 *
 *     public static void main(final String[] args) {
 *
 *         Microservice.run(SampleMS.class, args);
 *
 *     }
 * }
 * </pre>
 */
public class Microservice {

    protected static Class<? extends Application> applicationClass;

    /**
     * Microservice engines to use. Made protected so that it can be altered for
     * testing.
     */
    protected static Iterator<MicroserviceEngine> microserviceEngineIterator = ServiceLoader.load(MicroserviceEngine.class).iterator();

    /**
     * This returns the application class that was set, may be <code>null</code>.
     *
     * @return the application class.
     */
    public static Class<? extends Application> getApplicationClass() {

        return applicationClass;
    }

    /**
     * Bootstrap the microservice application.
     *
     * @param applicationClass
     *            JAX-RS Application class
     * @param extraSources
     *            extra source classes.
     * @param args
     *            command line arguments
     */
    public static void run(final Class<? extends Application> applicationClass,
        final Class<?>[] extraSources,
        final String... args) {

        if (!microserviceEngineIterator.hasNext()) {
            throw new LinkageError("No MicroserviceEngine was defined");
        }
        final MicroserviceEngine microserviceEngine = microserviceEngineIterator.next();
        if (microserviceEngineIterator.hasNext()) {
            throw new LinkageError(format("Multiple MicroserviceEngine was defined, %s, %s and possibly more", microserviceEngine, microserviceEngineIterator.next()));
        }
        if (Microservice.applicationClass != null) {
            throw new LinkageError("Another Application class has already been registered in this JVM.");
        }
        Microservice.applicationClass = applicationClass;

        final Object[] bootstrapObjects = microserviceEngine.bootstrap();
        final Object[] sources = new Object[extraSources.length + bootstrapObjects.length];

        System.arraycopy(extraSources, 0, sources, 0, extraSources.length);
        System.arraycopy(bootstrapObjects, 0, sources, extraSources.length, bootstrapObjects.length);

        final SpringApplication springApplication = new SpringApplication(sources);
        springApplication.setWebEnvironment(false);
        springApplication
            .setBannerMode(Mode.OFF);
        springApplication.run(args);
    }

    /**
     * Bootstrap the microservice application.
     *
     * @param applicationClass
     *            JAX-RS Application class
     * @param args
     *            command line arguments
     */
    public static void run(final Class<? extends Application> applicationClass,
        final String... args) {

        run(applicationClass, new Class<?>[0], args);
    }

    /**
     * Prevent instantiation. Only use {@link #run(Class, String...)}
     */
    protected Microservice() {

    }

}
