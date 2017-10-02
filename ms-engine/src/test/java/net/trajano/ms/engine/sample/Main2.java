package net.trajano.ms.engine.sample;

import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.internal.VertxBufferOutputStream;
import net.trajano.ms.engine.internal.resteasy.VertxRequestHandler;

public class Main2 extends AbstractVerticle {

    public static void main(final String[] args) throws Exception {

        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        Security.addProvider(new BouncyCastleProvider());
        final VertxOptions vertOptions = new VertxOptions();
        //        vertOptions.setMaxEventLoopExecuteTime(4000000000L);
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
        vertx.deployVerticle(new Main2(), options);
    }

    private VertxRequestHandler requestHandler;

    @Override
    public void start() throws Exception {

        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(2048);

        final KeyPair keyPair = generator.generateKeyPair();

        final RSAPrivateKey priv = (RSAPrivateKey) keyPair.getPrivate();
        final RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();

        final X500Name x500Principal = new X500Name("cn=example");
        final JcaX509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(x500Principal,
            BigInteger.valueOf(SecureRandom.getInstanceStrong().nextLong()),
            Date.from(Instant.now()), Date.from(Instant.now().plus(3650, ChronoUnit.DAYS)),
            x500Principal, pub);
        final X509CertificateHolder certificateHolder = certGen.build(new JcaContentSignerBuilder("SHA256WithRSA").build(priv));

        final PemObject privPem = new PemObject("PRIVATE KEY", priv.getEncoded());
        final PemObject certPem = new PemObject("CERTIFICATE", certificateHolder.getEncoded());

        final Buffer privBuf = Buffer.buffer();
        final Buffer certBuf = Buffer.buffer();
        try (PemWriter pw = new PemWriter(new OutputStreamWriter(new VertxBufferOutputStream(privBuf)))) {
            pw.writeObject(privPem);
        }

        try (PemWriter pw = new PemWriter(new OutputStreamWriter(new VertxBufferOutputStream(certBuf)))) {
            pw.writeObject(certPem);
        }

        final Router router = Router.router(vertx);

        //final HttpServerOptions httpServerOptions = new HttpServerOptions();

        final HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setPort(8900)
            .setUseAlpn(true)
            .setSsl(true)
            .setPemKeyCertOptions(new PemKeyCertOptions()
                .setCertValue(certBuf)
                .setKeyValue(privBuf));
        final HttpServer http = vertx.createHttpServer(httpServerOptions);

        requestHandler = new VertxRequestHandler(MyApp.class);
        router.route("/api/*")
            .useNormalisedPath(true)
            .handler(requestHandler);

        System.out.println(privBuf);
        System.out.println(certBuf);
        http.requestHandler(req -> router.accept(req)).listen(res -> {
            if (res.failed()) {
                res.cause().printStackTrace();
                vertx.close();
            }
        });
    }

    @Override
    public void stop() throws Exception {

        requestHandler.close();
    }

}
