package net.trajano.ms.gateway.providers;

import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.vertx.core.http.HttpClientRequest;

@Component
public class GatewayClientAuthorization {

    /**
     * This is the Authorization header value for the gateway when requesting the
     * JWT data from the authorization server.
     */
    private String authorizationHeader;

    /**
     * Gateway client ID. The gateway has it's own client ID because it is the only
     * one that should be authorized to get the id_token from an authorization_code
     * request to the authorization server token endpoint.
     */
    @Value("${authorization.client_id}")
    private String gatewayClientId;

    /**
     * Gateway client secret
     */
    @Value("${authorization.client_secret}")
    private String gatewayClientSecret;

    /**
     * Adds the authorization header to the request.
     *
     * @param request
     *            request
     * @return modified request
     */
    public HttpClientRequest gatewayRequest(final HttpClientRequest request) {

        return request.putHeader(AUTHORIZATION, authorizationHeader);
    }

    @PostConstruct
    public void init() {

        authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((gatewayClientId + ":" + gatewayClientSecret).getBytes(StandardCharsets.UTF_8));
    }

}
