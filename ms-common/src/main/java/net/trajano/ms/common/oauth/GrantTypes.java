package net.trajano.ms.common.oauth;

public final class GrantTypes {

    public static final String AUTHORIZATION_CODE = "authorization_code";

    public static final String CLIENT_CREDENTIALS = "client_credentials";

    public static final String JWT_ASSERTION = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    public static final String REFRESH_TOKEN = "refresh_token";

    private GrantTypes() {

    }
}
