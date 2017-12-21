package net.trajano.ms.engine.jaxrs.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.trajano.ms.engine.jaxrs.PathsProvider;
import net.trajano.ms.engine.jaxrs.sample.SampleResource;
import org.junit.Test;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.jaxrs.JaxRsRouter;
import net.trajano.ms.engine.jaxrs.sample.SampleApp;

import java.util.Arrays;

public class JaxRsRouterTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testRegister() throws Exception {

        final JaxRsRouter jaxRsRouter = new JaxRsRouter();
        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        final PathsProvider pathsProvider = mock(PathsProvider.class);
        when(pathsProvider.getPathAnnotatedClasses()).thenReturn(Arrays.asList(SampleResource.class));
        jaxRsRouter.register(SampleApp.class, router, pathsProvider, mock(Handler.class));
    }
}
