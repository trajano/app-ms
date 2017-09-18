package net.trajano.ms.oidc.internal;

import feign.RequestLine;
import net.trajano.ms.oidc.OpenIdConfiguration;

public interface WellKnownAPI {

    @RequestLine("GET /.well-known/openid-configuration")
    OpenIdConfiguration openIdConfiguration();//throws RestServiceException;
}
