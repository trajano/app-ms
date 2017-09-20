package ne.trajano.ms.engine.test;

import java.net.URI;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Test;
import org.mockito.Mockito;

import net.trajano.ms.engine.VertxSecurityContext;

public class JerseyTest {

    @Test
    public void foo() {

        //          AnnotationAcceptingListener.newJaxrsResourceAndProviderListener()
    }

    public void testF() {

        final ContainerRequest request = new ContainerRequest(URI.create("http://localhost:8181"), URI.create("http://localhost:8181/hello"), "GET", new VertxSecurityContext(), new MapPropertiesDelegate());
        final ExtendedUriInfo uriInfo = request.getUriInfo();
        request.setWriter(Mockito.mock(ContainerResponseWriter.class));
        new ApplicationHandler().handle(request);
        System.out.println(request.getWorkers());
        System.out.println(uriInfo.getPath());
        System.out.println(uriInfo.getMatchedResources());
    }
}
