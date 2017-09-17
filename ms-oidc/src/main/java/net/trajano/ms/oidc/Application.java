package net.trajano.ms.oidc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.wso2.msf4j.spring.MSF4JSpringApplication;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = {
    Application.class
})
public class Application {

    public static void main(final String[] args) {

        MSF4JSpringApplication.run(Application.class, args);

    }
}
