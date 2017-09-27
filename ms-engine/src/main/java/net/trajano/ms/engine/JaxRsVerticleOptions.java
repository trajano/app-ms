package net.trajano.ms.engine;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import io.vertx.core.http.HttpServerOptions;

@XmlRootElement
public class JaxRsVerticleOptions {

    private List<String> applicationClasses;

    private String certificatePath;

    private final HttpServerOptions http = new HttpServerOptions();

    private String keyPath;

    public JaxRsVerticleOptions(final String... applicationClasses) {

        http.setPort(8280);
        this.applicationClasses = Arrays.asList(applicationClasses);
    }

    public List<String> getApplicationClasses() {

        return applicationClasses;
    }

    public String getCertificatePath() {

        return certificatePath;
    }

    public HttpServerOptions getHttp() {

        return http;
    }

    public String getKeyPath() {

        return keyPath;
    }

    public boolean isKeyAndCertPathsPresent() {

        return certificatePath != null && keyPath != null;
    }

    public void setApplicationClasses(final List<String> applicationClasses) {

        this.applicationClasses = applicationClasses;
    }

    public void setCertificatePath(final String certificatePath) {

        this.certificatePath = certificatePath;
    }

    public void setKeyPath(final String keyPath) {

        this.keyPath = keyPath;
    }

}
