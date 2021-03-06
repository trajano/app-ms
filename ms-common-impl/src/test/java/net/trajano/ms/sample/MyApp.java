package net.trajano.ms.sample;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.trajano.ms.Microservice;

/**
 * This is the Jax-RS Application defintion. This is also where Spring
 * annotations can be configured. This will automatically scan for JAX-RS
 * resources in the package of the application. If you want to use Spring
 * components then {@link @SpringBootApplication} should be set.
 *
 * @author Archimedes Trajano
 */
@ApplicationPath("/api")
@SwaggerDefinition(
    info = @Info(title = "Test",
        version = "1.0"))
@SpringBootApplication
public class MyApp extends Application {

    public static void main(final String[] args) {

        Microservice.run(MyApp.class, args);
    }

    /**
     * Change some settings on the interceptor
     */
    @PostConstruct
    public void init() {

    }

}
