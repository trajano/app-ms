package net.trajano.ms.oidc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.wso2.msf4j.spring.MSF4JSpringApplication;

import net.trajano.ms.common.beans.CommonMs;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = {
    CommonMs.class,
    Application.class
})
public class Application {

    public static void main(final String[] args) {

        MSF4JSpringApplication.run(Application.class, args);

    }
}
