package net.trajano.ms.common.test;

import java.util.Arrays;

import javax.ws.rs.core.Application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import net.trajano.ms.Microservice;
import net.trajano.ms.spi.MicroserviceEngine;

public class BootstrapTest {

    private static class MyApp extends Application {
    }

    private static class TestMicroservice extends Microservice {

        public void setApplicationAndEngines(final Class<? extends Application> applicationClass,
            final MicroserviceEngine... engines) {

            microserviceEngineIterator = Arrays.asList(engines).iterator();
            Microservice.applicationClass = applicationClass;
        }

        public void setEngines(final MicroserviceEngine... engines) {

            microserviceEngineIterator = Arrays.asList(engines).iterator();
            applicationClass = null;
        }
    }

    @Test
    public void bootstrapTest() throws Exception {

        final MicroserviceEngine microserviceEngine = Mockito.mock(MicroserviceEngine.class);
        Mockito.when(microserviceEngine.bootstrap()).thenReturn(new Class<?>[] {
            MyApp.class
        });
        new TestMicroservice().setEngines(microserviceEngine);
        Microservice.run(MyApp.class);
        Assert.assertEquals(MyApp.class, Microservice.getApplicationClass());
    }

    @Test(expected = LinkageError.class)
    public void doubleRunTest() throws Exception {

        final MicroserviceEngine microserviceEngine = Mockito.mock(MicroserviceEngine.class);
        Mockito.when(microserviceEngine.bootstrap()).thenReturn(new Class<?>[] {
            MyApp.class
        });
        new TestMicroservice().setApplicationAndEngines(MyApp.class, microserviceEngine);
        Microservice.run(MyApp.class);
    }

    @Test(expected = LinkageError.class)
    public void noEngineTest() throws Exception {

        Microservice.run(MyApp.class);
    }

    @Before
    public void resetEngineLoader() {

        new TestMicroservice().setEngines();
    }

    @Test(expected = LinkageError.class)
    public void twoEngineTest() throws Exception {

        new TestMicroservice().setEngines(Mockito.mock(MicroserviceEngine.class), Mockito.mock(MicroserviceEngine.class));
        Microservice.run(MyApp.class);
    }

}
