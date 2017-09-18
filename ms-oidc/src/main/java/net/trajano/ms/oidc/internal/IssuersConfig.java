package net.trajano.ms.oidc.internal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.wso2.msf4j.client.MSF4JClient;

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

    /**
     * Performs the load of the issuer data and returns a map from ID to the issuer
     * config.
     */
    public Map<String, IssuerConfig> load() {

        issuers.forEach(issuer -> {
            issuer.setOpenIdConfiguration(new MSF4JClient.Builder<WellKnownAPI>().apiClass(WellKnownAPI.class).serviceEndpoint(issuer.getUri().toASCIIString()).build().api().openIdConfiguration());
        });
        return issuers.stream().collect(Collectors.toMap(IssuerConfig::getId, Function.identity()));

    }

    public void setIssuers(final List<IssuerConfig> issuers) {

        this.issuers = issuers;
    }
}
