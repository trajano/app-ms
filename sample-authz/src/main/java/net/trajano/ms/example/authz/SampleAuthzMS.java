package net.trajano.ms.example.authz;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.trajano.ms.Microservice;
import net.trajano.ms.authz.Authorization;
import net.trajano.ms.authz.jsonclientvalidator.JsonClientValidator;

@SpringBootApplication(
    scanBasePackageClasses = {
        SampleAuthzMS.class,
        JsonClientValidator.class,
        Authorization.class
    })

public class SampleAuthzMS extends Application {

    public static void main(final String[] args) {

        Microservice.run(SampleAuthzMS.class, args);
    }

}
