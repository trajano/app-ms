package net.trajano.ms.example.authz;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.Microservice;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SampleAuthzMS extends Application {

    public static void main(final String[] args) {

        Microservice.run(SampleAuthzMS.class, args);
    }

}
