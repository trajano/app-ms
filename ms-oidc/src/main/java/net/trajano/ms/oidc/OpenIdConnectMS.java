package net.trajano.ms.oidc;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.Microservice;

@SpringBootApplication
@EnableScheduling
public class OpenIdConnectMS extends Application {

    public static void main(final String[] args) throws Exception {

        Microservice.run(OpenIdConnectMS.class, args);

    }
}
