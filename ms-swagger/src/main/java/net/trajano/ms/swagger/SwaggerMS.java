package net.trajano.ms.swagger;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.trajano.ms.common.Microservice;

@SpringBootApplication
public class SwaggerMS extends Application {

    public static void main(final String[] args) {

        Microservice.run(SwaggerMS.class, args);
    }
}
