package net.trajano.ms.common.sample;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class SomeRequestScope {

    private final int i = ThreadLocalRandom.current().nextInt();

    public int get() {

        return i;
    }

}
