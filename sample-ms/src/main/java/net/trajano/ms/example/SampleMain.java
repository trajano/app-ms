package net.trajano.ms.example;

import javax.ws.rs.core.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.Microservice;

@SpringBootApplication
@EnableScheduling
@ComponentScan
public class SampleMain extends Application {

    public static void main(final String[] args) {

        Microservice.run(SampleMain.class, args);
    }
}
