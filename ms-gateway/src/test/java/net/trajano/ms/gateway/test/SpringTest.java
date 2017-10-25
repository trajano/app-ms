package net.trajano.ms.gateway.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.GatewayMS;
import net.trajano.ms.gateway.providers.RequestIDProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    GatewayMS.class,
    SpringTestConfiguration.class
})
public class SpringTest {

    @Autowired
    private RequestIDProvider requestIDProvider;

    @Autowired
    private Vertx vertx;

    @Test
    public void checkRequestId() {

        final RoutingContext routingContext = mock(RoutingContext.class);
        when(routingContext.response()).thenReturn(mock(HttpServerResponse.class));
        requestIDProvider.newRequestID(routingContext);
    }

    @Test
    public void exampleTest() {

        Assert.assertNotNull(vertx);
    }

}
