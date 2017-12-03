package net.trajano.ms.engine.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.engine.SpringJaxRsHandler;
import net.trajano.ms.engine.sample.MyApp;

@RunWith(VertxUnitRunner.class)
public class SpringJaxRsHandlerTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void test(final TestContext testContext) throws Exception {

        final Router router = Router.router(rule.vertx());
        final SpringJaxRsHandler handler = SpringJaxRsHandler.registerToRouter(router, MyApp.class);
        final RoutingContext routingContext = mock(RoutingContext.class);
        when(routingContext.currentRoute()).thenReturn(router.get("/api/hello"));
        when(routingContext.vertx()).thenReturn(rule.vertx());

        final HttpServerRequest serverRequest = mock(HttpServerRequest.class);
        when(serverRequest.absoluteURI()).thenReturn("/api/hello");
        when(serverRequest.isEnded()).thenReturn(true);
        when(serverRequest.method()).thenReturn(HttpMethod.GET);
        when(routingContext.request()).thenReturn(serverRequest);

        final HttpServerResponse response = mock(HttpServerResponse.class);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        when(response.headers()).thenReturn(new VertxHttpHeaders());

        when(routingContext.response()).thenReturn(response);

        handler.handle(routingContext);
        Thread.sleep(1000);
        verify(response, times(1)).setStatusCode(200);

        final ArgumentCaptor<Buffer> captor = ArgumentCaptor.forClass(Buffer.class);
        verify(response, times(1)).write(captor.capture());
        assertTrue(captor.getValue().toString().startsWith("Hello"));
    }

}
