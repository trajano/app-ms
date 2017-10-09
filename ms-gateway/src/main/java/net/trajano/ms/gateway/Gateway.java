package net.trajano.ms.gateway;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

@SpringBootApplication
public class Gateway {

    public static void main(final String[] args) {

        SpringApplication.run(Gateway.class, args);
    }

    @Autowired
    private GatewayVerticle gatewayVerticle;

    @Autowired
    private VertxOptions vertxOptions;

    @PostConstruct
    public void start() {

        Vertx.vertx(vertxOptions).deployVerticle(gatewayVerticle);
    }
}
