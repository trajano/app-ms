package net.trajano.ms.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.springframework.beans.factory.BeanExpressionException;

import net.trajano.ms.core.ErrorResponse;

public class ErrorResponseTest {

    @Test
    public void chainedError() {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem", new IllegalStateException()), mock(HttpHeaders.class), mock(UriInfo.class), true, false);
        assertNotNull(response.getStackTrace());
        assertNotNull(response.getCause());
    }

    /**
     * Chained three levels.
     */
    @Test
    public void chainedError2() {

        final ErrorResponse response = new ErrorResponse(new BeanExpressionException("ahem", new IllegalStateException(new GeneralSecurityException())), mock(HttpHeaders.class), mock(UriInfo.class), true, false);
        assertNotNull(response.getStackTrace());
        assertNotNull(response.getCause());
    }

    @Test
    public void chainedErrorButNoStackTrace() {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem", new IllegalStateException()), mock(HttpHeaders.class), mock(UriInfo.class), false, false);
        assertNull(response.getStackTrace());
        assertNull(response.getCause());
    }

    @Test
    public void errorWithNoStackTraces() {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem"), mock(HttpHeaders.class), mock(UriInfo.class), false, false);
        assertNull(response.getStackTrace());
        assertNull(response.getCause());
        assertEquals(Thread.currentThread().getName(), response.getThreadId());
        assertEquals(IOException.class.getName(), response.getErrorClass());
    }

    @Test
    public void simpleError() {

        final ErrorResponse response = new ErrorResponse("error", "error description", "request id");
        assertEquals("error", response.getError());
        assertEquals("error description", response.getErrorDescription());
        assertEquals("request id", response.getRequestId());
        assertNull(response.getStackTrace());
        assertNull(response.getCause());
    }

    @Test
    public void thrownError() {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem"), mock(HttpHeaders.class), mock(UriInfo.class), true, false);
        assertNotNull(response.getStackTrace());
        assertNull(response.getCause());
    }

}
