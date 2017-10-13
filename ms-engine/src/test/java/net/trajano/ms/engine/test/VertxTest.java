package net.trajano.ms.engine.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.ManifestHandler;

@RunWith(VertxUnitRunner.class)
public class VertxTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void testManifestRoute(final TestContext context) {

        final Router router = Router.router(rule.vertx());
        final ManifestHandler handler = ManifestHandler.registerToRouter(router);

        final RoutingContext routingContext = mock(RoutingContext.class);
        when(routingContext.currentRoute()).thenReturn(router.get("/.well-known/manifest"));
        when(routingContext.request()).thenReturn(mock(HttpServerRequest.class));
        final HttpServerResponse response = mock(HttpServerResponse.class);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        when(routingContext.response()).thenReturn(response);

        handler.handle(routingContext);
        verify(response, times(1)).end(any(Buffer.class));
    }

    @Test
    public void testManifestRouteText(final TestContext context) {

        final Router router = Router.router(rule.vertx());
        final ManifestHandler handler = ManifestHandler.registerToRouter(router);

        final RoutingContext routingContext = mock(RoutingContext.class);
        when(routingContext.currentRoute()).thenReturn(router.get("/.well-known/manifest"));
        when(routingContext.request()).thenReturn(mock(HttpServerRequest.class));
        when(routingContext.getAcceptableContentType()).thenReturn(MediaType.TEXT_PLAIN);
        final HttpServerResponse response = mock(HttpServerResponse.class);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        when(routingContext.response()).thenReturn(response);

        handler.handle(routingContext);
        verify(response, times(1)).end(any(Buffer.class));
    }

    /**
     * A simple vert.x test to show an example
     *
     * @param context
     *            test context.
     */
    @Test
    public void testSomething(final TestContext context) {

        context.assertFalse(false);
        context.assertNotNull(rule.vertx());
    }

}
