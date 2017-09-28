package net.trajano.ms.example;

import javax.ws.rs.core.Application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.trajano.ms.common.CommonMs;
import net.trajano.ms.common.MsEngineApplication;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = {
    CommonMs.class,
    SampleMain.class
})
public class SampleMain extends Application {

    public static void main(final String[] args) {

        MsEngineApplication.run(SampleMain.class);
    }
}
