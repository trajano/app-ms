package net.trajano.ms.spi;

public interface MicroserviceEngine {

    /**
     * Performs the initialization of the microservice engine and returns an array
     * of classes that would be used to bootstrap Spring.
     *
     * @return starting context classes.
     */
    Class<?>[] bootstrap();

    /**
     * Gets the host name of where the engine is running. This may be
     * <code>null</code> if the engine has not been initialized.
     *
     * @return host name
     */
    String hostname();

    /**
     * Gets the port of where the engine is listening. This may be <code>-1</code>
     * if the engine has not been initialized.
     *
     * @return host name
     */
    int port();

}
