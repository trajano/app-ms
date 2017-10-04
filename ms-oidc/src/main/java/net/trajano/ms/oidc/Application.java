package net.trajano.ms.oidc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.Microservice;

@Configuration
@EnableScheduling
@ComponentScan
public class Application extends javax.ws.rs.core.Application {

    public static void main(final String[] args) throws Exception {

        Microservice.run(Application.class, args);

    }
}
