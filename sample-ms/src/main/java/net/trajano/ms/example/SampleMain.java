package net.trajano.ms.example;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.wso2.msf4j.spring.MSF4JSpringApplication;

import net.trajano.ms.common.CommonMs;

@Configuration
@ComponentScan(basePackageClasses = CommonMs.class)
public class SampleMain {

    public static void main(final String[] args) {

        MSF4JSpringApplication.run(SampleMain.class, args);
    }
}
