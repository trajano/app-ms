package net.trajano.ms.gateway;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.vertx.core.Vertx;

@SpringBootApplication
public class Gateway {

    public static void main(final String[] args) {

        SpringApplication.run(Gateway.class, args);
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
