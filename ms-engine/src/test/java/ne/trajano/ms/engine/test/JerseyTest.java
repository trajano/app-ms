package ne.trajano.ms.engine.test;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Test;
import org.mockito.Mockito;

import net.trajano.ms.engine.sample.MyApp;

public class JerseyTest {

    @Test
    public void testMatching() {

        final ContainerRequest request = new ContainerRequest(URI.create("http://localhost:8181/"), URI.create("http://localhost:8181/hello"), "GET", Mockito.mock(SecurityContext.class), new MapPropertiesDelegate());
        final ExtendedUriInfo uriInfo = request.getUriInfo();
        request.setWriter(Mockito.mock(ContainerResponseWriter.class));
        new ApplicationHandler(MyApp.class).handle(request);
        assertEquals("hello", uriInfo.getPath());
        assertEquals(1, uriInfo.getMatchedResources().size());
    }
}
