package net.trajano.ms.example.oidc;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.trajano.ms.Microservice;
import net.trajano.ms.auth.jsonclientvalidator.JsonClientValidator;
import net.trajano.ms.oidc.OpenIdConnect;

@SpringBootApplication(
    scanBasePackageClasses = {
        SampleOidcMS.class,
        OpenIdConnect.class,
        JsonClientValidator.class
    })
public class SampleOidcMS extends Application {

    public static void main(final String[] args) {

        Microservice.run(SampleOidcMS.class, args);
    }
}
