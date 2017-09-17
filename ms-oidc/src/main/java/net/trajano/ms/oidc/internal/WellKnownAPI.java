package net.trajano.ms.oidc.internal;

import org.wso2.msf4j.client.exception.RestServiceException;

import feign.RequestLine;

public interface WellKnownAPI {

    @RequestLine("GET /.well-known/openid-configuration")
    OpenIdConfiguration openIdConfiguration() throws RestServiceException;
}
