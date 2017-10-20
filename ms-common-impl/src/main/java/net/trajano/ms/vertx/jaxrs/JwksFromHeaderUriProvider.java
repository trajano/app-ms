package net.trajano.ms.vertx.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.ws.rs.container.ContainerRequestContext;
import java.net.URI;

@Component
@Configuration
public class JwksFromHeaderUriProvider implements
    JwksUriProvider {

    @Autowired(required = false)
    @Qualifier("authz.signature.jwks.uri")
    private URI signatureJwksUri;

    private static final Logger LOG = LoggerFactory.getLogger(JwksFromHeaderUriProvider.class);

    @Override
    public URI getUri(ContainerRequestContext requestContext) {

        if (signatureJwksUri != null) {
            return signatureJwksUri;
        }
        final String jwksUriFromHeader = requestContext.getHeaderString("X-JWKS-URI");
        if (jwksUriFromHeader != null) {
            return URI.create(jwksUriFromHeader);
        } else {
            LOG.warn("Neither `authz.signature.jwks.uri` was specified nor `X-JWKS-URI` was found in the header, signature verification will not be performed");
            return null;
        }
    }
}
