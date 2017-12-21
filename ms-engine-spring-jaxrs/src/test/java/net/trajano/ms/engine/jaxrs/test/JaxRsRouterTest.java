package net.trajano.ms.engine.jaxrs.test;

import static org.mockito.Mockito.mock;

import net.trajano.ms.engine.jaxrs.PathsProvider;
import org.junit.Test;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.jaxrs.JaxRsRouter;
import net.trajano.ms.engine.jaxrs.sample.SampleApp;

public class JaxRsRouterTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testRegister() throws Exception {

        final JaxRsRouter jaxRsRouter = new JaxRsRouter();
        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        jaxRsRouter.register(SampleApp.class, router, mock(PathsProvider.class), mock(Handler.class));
    }
}
