package net.trajano.ms.common.test;

import net.trajano.ms.core.ErrorResponse;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class ErrorResponseTest {
    @Test
    public void errorWithNoStackTraces() {
        ErrorResponse response = new ErrorResponse(new IOException("ahem"), mock(HttpHeaders.class), mock(UriInfo.class), false, false);
        assertNull(response.getStackTrace());
        assertNull(response.getCause());
    }
    @Test
    public void chainedErrorButNoStackTrace() {
        ErrorResponse response = new ErrorResponse(new IOException("ahem", new IllegalStateException()), mock(HttpHeaders.class), mock(UriInfo.class), false, false);
        assertNull(response.getStackTrace());
        assertNull(response.getCause());
    }

    @Test
    public void thrownError() {
        ErrorResponse response = new ErrorResponse(new IOException("ahem"), mock(HttpHeaders.class), mock(UriInfo.class), true, false);
        assertNotNull(response.getStackTrace());
        assertNull(response.getCause());
    }

    @Test
    public void chainedError() {
        ErrorResponse response = new ErrorResponse(new IOException("ahem", new IllegalStateException()), mock(HttpHeaders.class), mock(UriInfo.class), true, false);
        assertNotNull(response.getStackTrace());
        assertNotNull(response.getCause());
    }

    @Test
    public void simpleError() {
        ErrorResponse response = new ErrorResponse("error", "error description", "request id");
        assertEquals("error", response.getError());
        assertEquals("error description", response.getErrorDescription());
        assertEquals("request id", response.getRequestId());
        assertNull(response.getStackTrace());
        assertNull(response.getCause());
    }

}
