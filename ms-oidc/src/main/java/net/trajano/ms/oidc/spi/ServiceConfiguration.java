package net.trajano.ms.oidc.spi;

import java.net.URI;

public interface ServiceConfiguration {

    /**
     * Gets an issuer configuration for a given issuer id
     * 
     * @param issuerId
     *            issuer ID
     * @return
     */
    IssuerConfig getIssuerConfig(String issuerId);

    /**
     * Authorization callback endpoint.
     *
     * @return
     */
    URI getRedirectUri();

}
