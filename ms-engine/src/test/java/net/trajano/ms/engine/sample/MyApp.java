package net.trajano.ms.engine.sample;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * This is the Jax-RS Application defintion. This is also where Spring
 * annotations can be configured. This will automatically scan for JAX-RS
 * resources in the package of the application. If you want to use Spring
 * components then {@link ComponentScan} should be set.
 *
 * @author Archimedes Trajano
 */
@ApplicationPath("/api")
@SwaggerDefinition(
    info = @Info(title = "Test",
        version = "1.0"))
@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = MyApp.class)
public class MyApp extends Application {

}
