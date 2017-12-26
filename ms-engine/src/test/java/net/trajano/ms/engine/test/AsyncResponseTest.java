package net.trajano.ms.engine.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.DefaultTextPlain;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.internal.VertxRoutingContextHttpHeaders;
import net.trajano.ms.engine.internal.resteasy.VertxAsynchronousResponse;

public class AsyncResponseTest {

    @Test
    public void testAsyncResponse() {

        final RoutingContext routingContext = mock(RoutingContext.class);
        final HttpServerRequest serverRequest = mock(HttpServerRequest.class);

        when(serverRequest.getHeader((CharSequence) "Accept")).thenReturn("text/plain");

        when(routingContext.request()).thenReturn(serverRequest);

        final HttpServerResponse serverResponse = mock(HttpServerResponse.class);
        when(serverResponse.headers()).thenReturn(new VertxHttpHeaders());
        when(routingContext.response()).thenReturn(serverResponse);

        final ResteasyProviderFactory providerFactory = new ResteasyProviderFactory();
        providerFactory.register(new DefaultTextPlain());
        providerFactory.register(StringTextStar.class);

        final HttpRequest request = Mockito.mock(HttpRequest.class);
        final HttpHeaders headers = new VertxRoutingContextHttpHeaders(routingContext);
        when(request.getHttpHeaders()).thenReturn(headers);
        //        when(headers.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.TEXT_PLAIN_TYPE));

        final AsyncResponse asyncResponse = new VertxAsynchronousResponse(providerFactory, request, routingContext);

        Assert.assertTrue(asyncResponse.isSuspended());
        asyncResponse.resume(Response.ok("hello").build());

    }

}
