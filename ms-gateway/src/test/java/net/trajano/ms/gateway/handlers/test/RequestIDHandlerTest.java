package net.trajano.ms.gateway.handlers.test;

import static net.trajano.ms.gateway.providers.RequestIDProvider.REQUEST_ID;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.GatewayMS;
import net.trajano.ms.gateway.handlers.RequestIDHandler;
import net.trajano.ms.gateway.test.SpringTestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    GatewayMS.class,
    SpringTestConfiguration.class
})
public class RequestIDHandlerTest {

    @Autowired
    private RequestIDHandler handler;

    @Test
    public void testHandler() {

        final HttpServerResponse response = mock(HttpServerResponse.class);
        final RoutingContext routingContext = mock(RoutingContext.class);
        when(routingContext.response()).thenReturn(response);
        handler.handle(routingContext);
        assertNotNull(MDC.get(REQUEST_ID));
    }
}
