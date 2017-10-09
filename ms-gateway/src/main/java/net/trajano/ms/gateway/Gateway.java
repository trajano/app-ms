package net.trajano.ms.gateway;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.vertx.core.Vertx;

@SpringBootApplication
@EnableAutoConfiguration
public class Gateway {

    public static void main(final String[] args) {

        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        final SpringApplication application = new SpringApplication(Gateway.class);
        application.setBannerMode(Mode.OFF);
        application.setWebEnvironment(false);
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
