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
public class RefreshTokenGrantHandler implements
    GrantHandler {

    @Autowired
    private TokenCache tokenCache;

    @Override
    public String getGrantTypeHandled() {

        return GrantTypes.REFRESH_TOKEN;
    }

    @Override
    public OAuthTokenResponse handler(final Client jaxRsClient,
        final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        final String refreshToken = form.getFirst("refresh_token");
        if (refreshToken == null) {
            throw OAuthTokenResponse.badRequest("invalid_request", "Missing refresh token");
        }

        return tokenCache.refresh(refreshToken);
    }

}
