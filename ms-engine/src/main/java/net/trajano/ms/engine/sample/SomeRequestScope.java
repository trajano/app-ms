package net.trajano.ms.engine.sample;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class SomeRequestScope {

    private final int i = ThreadLocalRandom.current().nextInt();

    public int get() {

        return i;
    }

    @PostConstruct
    public void init() {

        System.out.println("request" + this);
    }
}
