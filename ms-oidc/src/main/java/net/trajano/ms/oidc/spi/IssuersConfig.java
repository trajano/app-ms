package net.trajano.ms.oidc.spi;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssuersConfig {

    private List<IssuerConfig> issuers;

    public List<IssuerConfig> getIssuers() {

        return issuers;
    }

    public void setIssuers(final List<IssuerConfig> issuers) {

        this.issuers = issuers;
    }
}
