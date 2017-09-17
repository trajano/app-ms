package net.trajano.ms.oidc;

import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IssuersConfig {

    private List<IssuerConfig> issuers;

    /**
     * Gets the issuer.
     *
     * @param issuer
     * @return
     */
    public IssuerConfig getIssuer(final String issuer) {

        final Optional<IssuerConfig> first = issuers.stream().filter(issuerConfig -> {
            return issuer.equals(issuerConfig.getId());
        }).findFirst();
        if (first.isPresent()) {
            return first.get();
        } else {
            return null;
        }
    }

    public List<IssuerConfig> getIssuers() {

        return issuers;
    }

    public void setIssuers(final List<IssuerConfig> issuers) {

        this.issuers = issuers;
    }
}
