package net.trajano.ms.authz;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Authorization token endpoint.
 */
@Configuration
@EnableCaching
@EnableScheduling
public class Authorization {

    protected Authorization() {

    }
}
