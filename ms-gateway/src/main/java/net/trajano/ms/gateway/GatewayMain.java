package net.trajano.ms.gateway;

import javax.ws.rs.core.Application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.Microservice;
import net.trajano.ms.common.beans.CommonMs;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = {
    CommonMs.class,
    GatewayMain.class
})
public class GatewayMain extends Application {

    public static void main(final String[] args) {

        Microservice.run(GatewayMain.class, args);
    }
}
