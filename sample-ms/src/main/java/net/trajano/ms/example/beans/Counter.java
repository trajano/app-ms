package net.trajano.ms.example.beans;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Counter {

    private int c = 0;

    public int count() {

        return c++;
    }
}
