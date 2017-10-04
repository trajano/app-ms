package net.trajano.ms.common.test;

import java.text.ParseException;

import org.junit.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.common.JwksProvider;
import net.trajano.ms.common.JwtClaimsProcessor;
import net.trajano.ms.common.TokenGenerator;
import net.trajano.ms.common.jaxrs.JwtAssertionInterceptor;

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
        public Boolean apply(final JWTClaimsSet claims) {

            try {
                return claimValue.equals(claims.getStringClaim(claimName));
            } catch (final ParseException e) {
                throw new RuntimeException(e);
            }
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

        final JWTClaimsSet jwtClaims = JWTClaimsSet.parse("{\"typ\":\"https://example.com/register\"}");
        final JWSObject jws = new JWSObject(new JWSHeader(JWSAlgorithm.RS512), new Payload(jwtClaims.toString()));
        jws.sign(jwksProvider.getASigner());
        final String jwt = jws.serialize();
        System.out.println(jwt);
        //        final Request request = mock(Request.class);
        //        when(request.getHeader("X-JWT-Assertion")).thenReturn(jwt);
        //        final Response responder = mock(Response.class);
        //        final ServiceMethodInfo serviceMethodInfo = mock(ServiceMethodInfo.class);
        //        assertTrue(interceptor.preCall(request, responder, serviceMethodInfo));
    }
}
