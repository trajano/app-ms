package net.trajano.ms.gateway;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.vertx.core.Vertx;

@SpringBootApplication
@EnableAutoConfiguration
public class GatewayMS {

    public static void main(final String[] args) {

        System.setProperty("vertx.disableDnsResolver", "true");
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

        final File logbackFile = new File("logback.xml");
        if (logbackFile.exists()) {
            System.setProperty("logging.config", logbackFile.getAbsolutePath());
        }

        final SpringApplication application = new SpringApplication(GatewayMS.class);
        application.setBannerMode(Mode.OFF);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);

    }

    @Autowired
    private GatewayVerticle gatewayVerticle;

    @Autowired
    private Vertx vertx;

    @PostConstruct
    public void start() {

        vertx.deployVerticle(gatewayVerticle);
    }
}
