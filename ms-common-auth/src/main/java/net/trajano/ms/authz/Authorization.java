package net.trajano.ms.authz;

import net.trajano.ms.Microservice;
import net.trajano.ms.authz.internal.CacheNames;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.ws.rs.core.Application;

/**
 * Authorization token endpoint.
 */
@Configuration
@EnableCaching
@EnableScheduling
@ComponentScan
public class Authorization {

    public static void run(Class<? extends Application> applicationClass,
        String... args) {

        Microservice.run(applicationClass, new Class<?>[] {
            Authorization.class
        }, args);
    }

    private Authorization() {

    }
}
