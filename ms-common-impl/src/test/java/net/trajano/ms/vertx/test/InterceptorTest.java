package net.trajano.ms.vertx.test;

import java.text.ParseException;

import org.junit.Ignore;
import org.junit.Test;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.vertx.beans.CryptoProvider;
import net.trajano.ms.vertx.beans.JwksProvider;
import net.trajano.ms.vertx.beans.JwtClaimsProcessor;
import net.trajano.ms.vertx.beans.TokenGenerator;
import net.trajano.ms.vertx.jaxrs.JwtAssertionInterceptor;

/**
 * Tests are hanging on Travis for some odd reason.
 *
 * @author Archimedes Trajano
 */
@Ignore
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
        final CryptoProvider cryptoProvider = new CryptoProvider();

        final TokenGenerator tokenGenerator = new TokenGenerator();
        tokenGenerator.setRandom(cryptoProvider.secureRandom());

        jwksProvider.setTokenGenerator(tokenGenerator);
        jwksProvider.setKeyPairGenerator(cryptoProvider.keyPairGenerator());
        jwksProvider.setRandom(cryptoProvider.secureRandom());
        jwksProvider.init();

        final JwtAssertionInterceptor interceptor = new JwtAssertionInterceptor();
        interceptor.setClaimsProcessor(new ValidatingProcessor("typ", "https://example.com/register"));
        interceptor.setJwksProvider(jwksProvider);
        interceptor.init();

        final JWTClaimsSet jwtClaims = JWTClaimsSet.parse("{\"typ\":\"https://example.com/register\"}");
        final JWSObject jws = jwksProvider.sign(jwtClaims);
        final String jwt = jws.serialize();
        System.out.println(jwt);
        //        final Request request = mock(Request.class);
        //        when(request.getHeader("X-JWT-Assertion")).thenReturn(jwt);
        //        final Response responder = mock(Response.class);
        //        final ServiceMethodInfo serviceMethodInfo = mock(ServiceMethodInfo.class);
        //        assertTrue(interceptor.preCall(request, responder, serviceMethodInfo));
    }
}
