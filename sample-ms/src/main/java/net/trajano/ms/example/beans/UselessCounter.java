package net.trajano.ms.example.beans;

import javax.enterprise.context.RequestScoped;

/**
 * This counter is useless and will always return 0 since it gets recreated for
 * every request.
 *
 * @author Archimedes Trajano
 */
@RequestScoped
public class UselessCounter {

    private int c = 0;

    public int count() {

        return c++;
    }
}
