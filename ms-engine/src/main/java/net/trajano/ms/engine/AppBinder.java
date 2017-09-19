package net.trajano.ms.engine;

import org.glassfish.jersey.internal.inject.AbstractBinder;

public class AppBinder extends AbstractBinder {

    @Override
    protected void configure() {

        //        new CdiSeInjectionManagerFactory().create()

        // bind(CdiSeInjectionManagerFactory.class).to(InjectionManagerFactory.class);

    }

}
