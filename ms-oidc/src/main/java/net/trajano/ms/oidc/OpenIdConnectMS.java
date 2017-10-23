package net.trajano.ms.oidc;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import net.trajano.ms.Microservice;

@SpringBootApplication
@EnableCaching
public class OpenIdConnectMS extends Application {

    public static void main(final String[] args) throws Exception {

        Microservice.run(OpenIdConnectMS.class, args);

    }
}
