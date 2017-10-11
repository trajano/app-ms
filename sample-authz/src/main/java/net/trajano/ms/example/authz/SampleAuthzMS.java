package net.trajano.ms.example.authz;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.Microservice;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SampleAuthzMS extends Application {

    public static void main(final String[] args) {

        Microservice.run(SampleAuthzMS.class, args);
    }
}
