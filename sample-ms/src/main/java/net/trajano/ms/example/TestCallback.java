package net.trajano.ms.example;

import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class TestCallback implements
    CompletionCallback {

    private static final Logger LOG = LoggerFactory.getLogger(TestCallback.class);

    @Override
    public void onComplete(final Throwable throwable) {

        LOG.debug("Complete throwable={}", throwable, throwable);

    }

}
