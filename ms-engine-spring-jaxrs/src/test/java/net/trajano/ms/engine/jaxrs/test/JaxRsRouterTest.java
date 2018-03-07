package net.trajano.ms.engine.jaxrs.test;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import net.trajano.ms.engine.jaxrs.JaxRsPath;
import net.trajano.ms.engine.jaxrs.JaxRsRouter;
import net.trajano.ms.engine.jaxrs.PathsProvider;
import net.trajano.ms.engine.jaxrs.sample.SampleApp;
import net.trajano.ms.engine.jaxrs.sample.SampleResource;

public class JaxRsRouterTest {

    @Test
    public void equalityTests() {

        final JaxRsPath p1 = new JaxRsPath("/hello", new String[] {
            MediaType.APPLICATION_JSON
        }, new String[] {
            MediaType.APPLICATION_JSON
        }, HttpMethod.POST);

        final JaxRsPath p2 = new JaxRsPath("/hello", new String[0], new String[] {
            MediaType.TEXT_PLAIN
        }, HttpMethod.GET);

        final JaxRsPath p3 = new JaxRsPath("/hello", new String[] {
            MediaType.APPLICATION_FORM_URLENCODED
        }, new String[] {
            MediaType.TEXT_PLAIN
        }, HttpMethod.POST);

        assertFalse(p1.equals(p2));
        assertFalse(p1.compareTo(p2) == 0);

        assertFalse(p1.equals(p3));
        assertFalse(p1.compareTo(p3) == 0);

        assertFalse(p2.equals(p3));
        assertFalse(p2.compareTo(p3) == 0);
        assertFalse(p3.compareTo(p1) == 0);

    }

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
