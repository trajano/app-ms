package net.trajano.ms.example.authz;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import net.trajano.ms.common.oauth.GrantHandler;
import net.trajano.ms.common.oauth.GrantTypes;
import net.trajano.ms.common.oauth.OAuthTokenResponse;

@Component
@Configuration
public class AuthorizationCodeGrantHandler implements
    GrantHandler {

    @Autowired
    private TokenCache tokenCache;

    @Override
    public String getGrantTypeHandled() {

        return GrantTypes.AUTHORIZATION_CODE;
    }

    @Override
    public OAuthTokenResponse handler(final Client jaxRsClient,
        final String clientId,
        final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        final String accessToken = form.getFirst("code");
        if (accessToken == null) {
            throw OAuthTokenResponse.badRequest("invalid_request", "Missing code");
        }

        return tokenCache.get(accessToken, clientId);
    }

}
