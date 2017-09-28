package net.trajano.ms.engine.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import net.trajano.ms.engine.JaxRsVerticle;
import net.trajano.ms.engine.JaxRsVerticleOptions;
import net.trajano.ms.engine.second.MyApp2;

public class Main extends AbstractVerticle {

    public static void main(final String[] args) {

        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        final VertxOptions vertOptions = new VertxOptions();
        vertOptions.setMaxEventLoopExecuteTime(4000000000L);
        vertOptions.setWarningExceptionTime(1);
        vertOptions.setWorkerPoolSize(50);
        //        final VertxOptions options = new VertxOptions();
        //        Vertx.clusteredVertx(options, event -> {
        //            final Vertx vertx = event.result();
        //            vertx.deployVerticle(new Main());
        //
        //        });

        final Vertx vertx = Vertx.vertx(vertOptions);
        final DeploymentOptions options = new DeploymentOptions();
        final JaxRsVerticleOptions jaxRsVerticleOptions = new JaxRsVerticleOptions(MyApp.class.getName(), MyApp2.class.getName());
        jaxRsVerticleOptions.setCertificatePath("cert.pem");
        jaxRsVerticleOptions.setKeyPath("key.pem");
        jaxRsVerticleOptions.getHttp()
            .setSsl(true)
            .setUseAlpn(true);
        options.setConfig(JsonObject.mapFrom(jaxRsVerticleOptions));
        vertx.deployVerticle(JaxRsVerticle.class.getName(), options);

    }

}
