package net.trajano.ms.common.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.junit.Test;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import net.trajano.ms.common.JwtClaimsProcessor;
import net.trajano.ms.common.TokenGenerator;
import net.trajano.ms.common.internal.JwksProvider;
import net.trajano.ms.common.internal.JwtAssertionInterceptor;

public class InterceptorTest {

    private static final class ValidatingProcessor implements
        JwtClaimsProcessor {

        final String claimName;

        final String claimValue;

        ValidatingProcessor(final String claimName,
            final String claimValue) {

            this.claimName = claimName;
            this.claimValue = claimValue;
        }

        @Override
        public boolean validateClaims(final JwtClaims claims) {

            return claimValue.equals(claims.getClaimsMap().get(claimName));
        }
    }

    @Test
    public void testInterceptor() throws Exception {

        final JwksProvider jwksProvider = new JwksProvider();
        final TokenGenerator tokenGenerator = new TokenGenerator();
        tokenGenerator.init();
        jwksProvider.setTokenGenerator(tokenGenerator);
        jwksProvider.init();

        final JwtAssertionInterceptor interceptor = new JwtAssertionInterceptor();
        interceptor.setClaimsProcessor(new ValidatingProcessor("typ", "https://example.com/register"));
        interceptor.setJwksProvider(jwksProvider);
        interceptor.init();

        final JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setClaim("typ", "https://example.com/register");
        final JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(jwtClaims.toJson());
        final RsaJsonWebKey aSigningKey = jwksProvider.getASigningKey();
        jws.setKey(aSigningKey.getPrivateKey());
        jws.setKeyIdHeaderValue(aSigningKey.getKeyId());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
        final String jwt = jws.getCompactSerialization();
        System.out.println(jwt);
        final Request request = mock(Request.class);
        when(request.getHeader("X-JWT-Assertion")).thenReturn(jwt);
        final Response responder = mock(Response.class);
        final ServiceMethodInfo serviceMethodInfo = mock(ServiceMethodInfo.class);
        assertTrue(interceptor.preCall(request, responder, serviceMethodInfo));
    }
}
