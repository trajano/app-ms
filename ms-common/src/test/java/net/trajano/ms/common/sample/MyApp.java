package net.trajano.ms.common.sample;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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

        run(Vertx.vertx(), MyApp.class);
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

    public static void run(final Vertx vertx,
        final Class<? extends Application> applicationClass) {

        final ConfigStoreOptions yamlStore = new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject()
                .put("path", ".")
                .put("filesets", new JsonArray()
                    .add(new JsonObject()
                        .put("pattern", "application.yml")
                        .put("format", "yaml"))));

        final ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
            .addStore(yamlStore);
        final ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);

        configRetriever.getConfig(r -> {
            if (r.succeeded()) {
                System.out.println("HERE");
                System.out.println(r.result());
            }

        });
        configRetriever.listen(event -> {

            System.out.println(vertx.deploymentIDs());
            System.out.println(event.getNewConfiguration());
        });

    }

}
