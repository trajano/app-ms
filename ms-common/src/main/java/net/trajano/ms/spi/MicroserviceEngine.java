package net.trajano.ms.spi;

public interface MicroserviceEngine {

    /**
     * Performs the initialization of the microservice engine and returns an
     * array of objects that would be used to bootstrap Spring.
     *
     * @return starting context objects.
     */
    Object[] bootstrap();

}
