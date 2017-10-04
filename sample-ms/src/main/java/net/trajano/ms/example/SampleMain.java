package net.trajano.ms.example;

import javax.ws.rs.core.Application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.Microservice;

@Configuration
@EnableScheduling
@ComponentScan
public class SampleMain extends Application {

    public static void main(final String[] args) throws Exception {

        Microservice.run(SampleMain.class, args);
    }
}
