package net.trajano.ms.engine.sample;

import java.util.concurrent.ThreadLocalRandom;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SomeAppScope implements
    ISomeAppScope {

    private final int i = ThreadLocalRandom.current().nextInt();

    @Override
    public int get() {

        return i;
    }
}
