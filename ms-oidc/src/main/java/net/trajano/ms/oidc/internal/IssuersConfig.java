package net.trajano.ms.oidc.internal;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IssuersConfig {

    private List<IssuerConfig> issuers;

    public List<IssuerConfig> getIssuers() {

        return issuers;
    }

    public void setIssuers(final List<IssuerConfig> issuers) {

        this.issuers = issuers;
    }
}
