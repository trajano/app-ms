package net.trajano.ms.engine.sample;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class SomeAppScope implements
    ISomeAppScope {

    private final int i = ThreadLocalRandom.current().nextInt();

    @Override
    public int get() {

        return i;
    }

    //@Scheduled(fixedRate = 10000)
    public void work() {

        System.gc();
        System.out.println("mem=" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }
}
