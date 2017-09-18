package net.trajano.ms.common;

public class AssertionNotRequiredFunction implements
    JwtAssertionRequiredFunction {

    @Override
    public Boolean apply(final String uri) {

        return false;
    }

}
