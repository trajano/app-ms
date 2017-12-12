package net.trajano.ms.vertx.test;

import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.sample.Hello;
import net.trajano.ms.vertx.VertxConfig;
import net.trajano.ms.vertx.beans.JwtClaimsProcessor;
import net.trajano.ms.vertx.jaxrs.JwtAssertionInterceptor;
import net.trajano.ms.vertx.jaxrs.MDCInterceptor;

/**
 * Tests are hanging on Travis for some odd reason.
 *
 * @author Archimedes Trajano
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    VertxConfig.class
})
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
        public Boolean apply(final JwtClaims claims) {

            try {
                return claimValue.equals(claims.getStringClaimValue(claimName));
            } catch (final MalformedClaimException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Autowired
    private CryptoOps cryptoOps;

    @Autowired
    private JwtAssertionInterceptor jwtInterceptor;

    @Autowired
    private MDCInterceptor mdcInterceptor;

    @SuppressWarnings({
        "unchecked",
        "rawtypes"
    })
    @Test
    public void testInterceptor() throws Exception {

        jwtInterceptor.setClaimsProcessor(new ValidatingProcessor("typ", "https://example.com/register"));

        final JwtClaims jwtClaims = JwtClaims.parse("{\"typ\":\"https://example.com/register\", \"aud\":\"sample\", \"jti\": \"abc\", \"iss\":\"http://accounts.trajano.net\"}");
        final String jwt = cryptoOps.sign(jwtClaims);
        System.out.println(jwt);

        final ResourceInfo resourceInfo = Mockito.mock(ResourceInfo.class);
        Mockito.when(resourceInfo.getResourceMethod()).thenReturn(Hello.class.getMethod("hello2B"));
        Mockito.when(resourceInfo.getResourceClass()).thenReturn((Class) Hello.class);
        jwtInterceptor.setResourceInfo(resourceInfo);

        final ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        final UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://trajano.net/sample"));
        Mockito.when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        Mockito.when(containerRequestContext.getHeaderString("X-JWT-Assertion")).thenReturn(jwt);
        Mockito.when(containerRequestContext.getHeaderString("X-JWT-Audience")).thenReturn("sample");
        mdcInterceptor.filter(containerRequestContext);
        jwtInterceptor.filter(containerRequestContext);
        //        final Request request = mock(Request.class);
        //        when(request.getHeader("X-JWT-Assertion")).thenReturn(jwt);
        //        final Response responder = mock(Response.class);
        //        final ServiceMethodInfo serviceMethodInfo = mock(ServiceMethodInfo.class);
        //        assertTrue(interceptor.preCall(request, responder, serviceMethodInfo));
    }
}
