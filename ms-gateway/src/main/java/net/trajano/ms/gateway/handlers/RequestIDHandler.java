package net.trajano.ms.gateway.handlers;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.providers.RequestIDProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_GLOBAL)
public class RequestIDHandler implements
    SelfRegisteringRoutingContextHandler {

    @Autowired
    private RequestIDProvider requestIDProvider;

    public void register(Router router) {

        router.route().handler(this);
    }

    @Override
    public void handle(RoutingContext context) {

        requestIDProvider.newRequestID(context);
    }
}
