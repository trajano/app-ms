package net.trajano.ms.vertx.test;

import javax.ws.rs.core.Application;

import net.trajano.ms.Microservice;

/**
 * This class extends Microservice so that it can change the application class
 * without starting the application. In addition it provides the component
 * loader that will load the implementation classes.
 *
 * @author Archimedes Trajano
 */
public class MicroserviceTestUtil extends Microservice {

    public static void setApplicationClass(final Class<? extends Application> applicationClass) {

        Microservice.applicationClass = applicationClass;
    }

}
