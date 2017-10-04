package net.trajano.ms.common;

import javax.ws.rs.container.ResourceInfo;

public class AssertionNotRequiredFunction implements
    JwtAssertionRequiredFunction {

    @Override
    public boolean test(final ResourceInfo context) {

        return false;
    }

}
