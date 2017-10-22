package net.trajano.ms.authz;

import javax.ws.rs.core.Application;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.Microservice;

/**
 * Authorization token endpoint.
 */
@Configuration
@EnableCaching
@EnableScheduling
@ComponentScan
public class Authorization {

    public static void run(final Class<? extends Application> applicationClass,
        final String... args) {

        Microservice.run(applicationClass, new Class<?>[] {
            Authorization.class
        }, args);
    }

    private Authorization() {

    }
}
