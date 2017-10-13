package net.trajano.ms.engine.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import net.trajano.ms.engine.SwaggerHandler;
import net.trajano.ms.engine.sample.MyApp;

@RunWith(VertxUnitRunner.class)
public class SwaggerRouteTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void testSwaggerRoute(final TestContext context) {

        final Router router = Router.router(rule.vertx());
        final SwaggerHandler handler = SwaggerHandler.registerToRouter(router, MyApp.class);

        context.assertNotNull(handler.getSwagger());
        final RoutingContext routingContext = mock(RoutingContext.class);
        when(routingContext.currentRoute()).thenReturn(router.get("/api"));
        when(routingContext.request()).thenReturn(mock(HttpServerRequest.class));
        final HttpServerResponse response = mock(HttpServerResponse.class);
        when(response.putHeader(any(CharSequence.class), any(CharSequence.class))).thenReturn(response);
        when(routingContext.response()).thenReturn(response);

        handler.handle(routingContext);
        verify(response, times(1)).end(any(Buffer.class));
    }
}
