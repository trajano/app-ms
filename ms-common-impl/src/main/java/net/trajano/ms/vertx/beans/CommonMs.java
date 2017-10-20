package net.trajano.ms.vertx.beans;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public interface CommonMs {

    public static final String JWKS_CACHE = "jwks_cache";
}
