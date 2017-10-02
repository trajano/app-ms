package net.trajano.ms.engine.internal.resteasy;

import org.jboss.resteasy.plugins.server.embedded.EmbeddedJaxrsServer;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class SpringVertxServer implements
    EmbeddedJaxrsServer {

    private ResteasyDeployment deployment;

    private String rootResourcePath;

    private SecurityDomain sc;

    @Override
    public ResteasyDeployment getDeployment() {

        return deployment;
    }

    @Override
    public void setDeployment(final ResteasyDeployment deployment) {

        this.deployment = deployment;

    }

    @Override
    public void setRootResourcePath(final String rootResourcePath) {

        this.rootResourcePath = rootResourcePath;

    }

    @Override
    public void setSecurityDomain(final SecurityDomain sc) {

        this.sc = sc;

    }

    @Override
    public void start() {

        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {

        // TODO Auto-generated method stub

    }

}
