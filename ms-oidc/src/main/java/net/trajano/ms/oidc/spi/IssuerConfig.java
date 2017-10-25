package net.trajano.ms.oidc.spi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.oidc.OpenIdConfiguration;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssuerConfig {

    @XmlElement(name = "client_id",
        required = true)
    private String clientId;

    @XmlElement(name = "client_secret",
        required = true)
    private String clientSecret;

    @XmlElement(name = "display")
    private String display;

    @XmlElement(required = true)
    private String id;

    @XmlTransient
    private HttpsJwks jwkset;

    @XmlTransient
    private OpenIdConfiguration openIdConfiguration;

    @XmlElement(name = "prompt")
    private String prompt;

    @XmlElement(name = "redirect_uri",
        required = true)
    private URI redirectUri;

    @XmlElement(name = "scope",
        required = true)
    private String scope;

    @XmlElement(required = true)
    private URI uri;

    public URI buildAuthenticationRequestUri(final URI redirectUri,
        final String state,
        final String nonce) {

        openIdConfiguration.getAuthorizationEndpoint();
        final UriBuilder b = UriBuilder.fromUri(openIdConfiguration.getAuthorizationEndpoint())
            .queryParam("response_type", "code")
            .queryParam("scope", scope)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("nonce", nonce);
        if (state != null) {
            b.queryParam("state", state);
        }
        if (display != null) {
            b.queryParam("display", display);
        }
        if (prompt != null) {
            b.queryParam("prompt", prompt);
        }
        return b
            .build();
    }

    public String buildAuthorization() {

        return HttpAuthorizationHeaders.buildBasicAuthorization(clientId, clientSecret);
    }

    public String getClientId() {

        return clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public String getDisplay() {

        return display;
    }

    public String getId() {

        return id;
    }

    public JsonWebKeySet getJwks() {

        try {
            if (jwkset == null) {
                jwkset = new HttpsJwks(getOpenIdConfiguration().getJwksUri().toASCIIString());
            }
            return new JsonWebKeySet(jwkset.getJsonWebKeys());
        } catch (final JoseException e) {
            throw new InternalServerErrorException(e);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OpenIdConfiguration getOpenIdConfiguration() {

        return openIdConfiguration;
    }

    public String getPrompt() {

        return prompt;
    }

    public URI getRedirectUri() {

        return redirectUri;
    }

    public String getScope() {

        return scope;
    }

    public URI getUri() {

        return uri;
    }

    public void setClientId(final String clientId) {

        this.clientId = clientId;
    }

    public void setClientSecret(final String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public void setDisplay(final String display) {

        this.display = display;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public void setOpenIdConfiguration(final OpenIdConfiguration openIdConfiguration) {

        this.openIdConfiguration = openIdConfiguration;
    }

    public void setPrompt(final String prompt) {

        this.prompt = prompt;
    }

    public void setRedirectUri(final URI redirectUri) {

        this.redirectUri = redirectUri;
    }

    public void setScope(final String scope) {

        this.scope = scope;
    }

    public void setUri(final URI uri) {

        this.uri = uri;
    }
}
