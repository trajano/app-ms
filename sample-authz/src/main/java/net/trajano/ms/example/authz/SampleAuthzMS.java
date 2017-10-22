package net.trajano.ms.example.authz;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import io.swagger.annotations.Authorization;
import net.trajano.ms.Microservice;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
    SampleAuthzMS.class,
    Authorization.class
})
public class SampleAuthzMS extends Application {

    public static void main(final String[] args) {

        Microservice.run(SampleAuthzMS.class, args);
    }

}
