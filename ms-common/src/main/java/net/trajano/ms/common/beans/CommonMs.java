package net.trajano.ms.common.beans;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class CommonMs {

    public static final String JWKS_CACHE = "jwks_cache";
}
