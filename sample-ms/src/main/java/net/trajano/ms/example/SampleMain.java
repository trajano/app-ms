package net.trajano.ms.example;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.wso2.msf4j.spring.MSF4JSpringApplication;

import net.trajano.ms.common.CommonMs;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = {
    CommonMs.class,
    SampleMain.class
})
public class SampleMain {

    public static void main(final String[] args) {

        MSF4JSpringApplication.run(SampleMain.class, args);
    }
}
