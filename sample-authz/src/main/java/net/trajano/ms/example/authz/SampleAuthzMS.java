package net.trajano.ms.example.authz;

import net.trajano.ms.Microservice;
import net.trajano.ms.authz.Authorization;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.ws.rs.core.Application;

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
