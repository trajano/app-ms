package net.trajano.ms.gateway;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.wso2.msf4j.spring.MSF4JSpringApplication;

import net.trajano.ms.common.beans.CommonMs;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = {
    CommonMs.class,
    GatewayMain.class
})
public class GatewayMain {

    public static void main(final String[] args) {

        MSF4JSpringApplication.run(GatewayMain.class, args);
    }
}
