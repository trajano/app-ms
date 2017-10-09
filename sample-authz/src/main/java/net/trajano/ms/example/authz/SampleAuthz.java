package net.trajano.ms.example.authz;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.Microservice;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties
@EnableScheduling
public class SampleAuthz extends Application {

    public static void main(final String[] args) {

        Microservice.run(SampleAuthz.class, args);
    }
}
