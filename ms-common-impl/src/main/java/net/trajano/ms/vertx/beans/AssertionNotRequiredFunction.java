package net.trajano.ms.vertx.beans;

import javax.ws.rs.container.ResourceInfo;

public class AssertionNotRequiredFunction implements
    JwtAssertionRequiredPredicate {

    @Override
    public boolean test(final ResourceInfo context) {

        return false;
    }

}
