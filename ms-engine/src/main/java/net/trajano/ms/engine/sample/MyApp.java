package net.trajano.ms.engine.sample;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
@ComponentScan
public class MyApp extends Application {

    @PostConstruct
    public void init() {

        System.out.println("nmaya apFFF");
    }
}
