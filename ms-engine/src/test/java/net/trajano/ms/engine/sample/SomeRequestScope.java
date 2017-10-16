package net.trajano.ms.engine.sample;

import java.util.concurrent.ThreadLocalRandom;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class SomeRequestScope {

    private final int i = ThreadLocalRandom.current().nextInt();

    public int get() {

        return i;
    }

}
