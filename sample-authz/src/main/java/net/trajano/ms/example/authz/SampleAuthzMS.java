package net.trajano.ms.example.authz;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.trajano.ms.Microservice;
import net.trajano.ms.authz.Authorization;

@SpringBootApplication(
    scanBasePackageClasses = {
        SampleAuthzMS.class,
        Authorization.class
    })
public class SampleAuthzMS extends Application {

    public static void main(final String[] args) {

        Microservice.run(SampleAuthzMS.class, args);
    }

}
