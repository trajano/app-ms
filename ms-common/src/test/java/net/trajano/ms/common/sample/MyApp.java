package net.trajano.ms.common.sample;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import net.trajano.ms.common.CommonMs;
import net.trajano.ms.common.MicroserviceVerticle;

/**
 * This is the Jax-RS Application defintion. This is also where Spring
 * annotations can be configured. This will automatically scan for JAX-RS
 * resources in the package of the application. If you want to use Spring
 * components then {@link ComponentScan} should be set.
 *
 * @author Archimedes Trajano
 */
@ApplicationPath("/api")
@SwaggerDefinition(
    info = @Info(title = "Test",
        version = "1.0"))
@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = MyApp.class)
public class MyApp extends Application {

    public static void main(final String[] args) throws Exception {

        final Object[] sources = new Object[] {
            CommonMs.class
        };
        SpringApplication.run(sources, args);
    }

    public static void main2(final String[] args) throws Exception {

        final VertxOptions vertOptions = new VertxOptions();
        vertOptions.setWarningExceptionTime(1);
        vertOptions.setWorkerPoolSize(50);

        final Vertx vertx = Vertx.vertx(vertOptions);
        final DeploymentOptions options = new DeploymentOptions();
        options.setConfig(new JsonObject()
            .put("application_class", MyApp.class.getName())
            .put("http", new JsonObject().put("port", 8900)));
        vertx.deployVerticle(new MicroserviceVerticle(), options);
    }

}
