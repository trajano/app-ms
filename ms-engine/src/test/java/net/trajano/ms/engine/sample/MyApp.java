package net.trajano.ms.engine.sample;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is the Jax-RS Application defintion. This is also where Spring
 * annotations can be configured. This will automatically scan for JAX-RS
 * resources in the package of the application. If you want to use Spring
 * components then {@link ComponentScan} should be set.
 *
 * @author Archimedes Trajano
 */
@ApplicationPath("/api")
@Configuration
@EnableScheduling
@ComponentScan
public class MyApp extends Application {

}
