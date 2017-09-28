package net.trajano.ms.common;

import javax.ws.rs.container.ContainerRequestContext;

public class AssertionNotRequiredFunction implements
    JwtAssertionRequiredFunction {

    @Override
    public Boolean apply(final ContainerRequestContext context) {

        return false;
    }

}
