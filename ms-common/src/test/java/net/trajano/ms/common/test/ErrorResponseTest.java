package net.trajano.ms.common.test;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.security.GeneralSecurityException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXB;

import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.BeanExpressionException;

import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.core.ErrorResponse;
import net.trajano.ms.core.ErrorResponses;
import net.trajano.ms.core.Qualifiers;
import net.trajano.ms.spi.CacheNames;
import net.trajano.ms.spi.MDCKeys;

/**
 * Tests {@link ErrorResponses}.
 *
 * @author Archimedes Trajano
 */
public class ErrorResponseTest {

    @Test
    public void assertConstClass() throws Exception {

        assertUtilityClassWellDefined(CacheNames.class);
        assertUtilityClassWellDefined(ErrorCodes.class);
        assertUtilityClassWellDefined(MDCKeys.class);
        assertUtilityClassWellDefined(Qualifiers.class);

    }

    @Test
    public void chainedError() {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem", new IllegalStateException()), mock(UriInfo.class), true);
        assertNotNull(response.getStackTrace());
        assertNotNull(response.getCause());
    }

    /**
     * Chained three levels.
     */
    @Test
    public void chainedError2() {

        final ErrorResponse response = new ErrorResponse(new BeanExpressionException("ahem", new IllegalStateException(new GeneralSecurityException())), mock(UriInfo.class), true);
        assertNotNull(response.getStackTrace());
        assertNotNull(response.getCause());
    }

    @Test
    public void chainedErrorButNoStackTrace() {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem", new IllegalStateException()), mock(UriInfo.class), false);
        assertNull(response.getStackTrace());
        assertNull(response.getCause());
    }

    @Test
    public void errorWithNoStackTraces() {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem"), mock(UriInfo.class), false);
        assertNull(response.getStackTrace());
        assertNull(response.getCause());
        assertEquals(Thread.currentThread().getName(), response.getThreadId());
        assertEquals(IOException.class.getName(), response.getErrorClass());
    }

    @Test
    public void jaxbTest() throws Exception {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem"), mock(UriInfo.class), true);
        final StringWriter writer = new StringWriter();
        JAXB.marshal(response, writer);
        final ErrorResponse unmarshaled = JAXB.unmarshal(new StringReader(writer.toString()), ErrorResponse.class);
        assertEquals(response.getError(), unmarshaled.getError());

    }

    @Test
    public void testConstructor() {

        final ErrorResponse response = new ErrorResponse("error", "error_description");
        assertEquals("error", response.getError());
        assertEquals("error_description", response.getErrorDescription());

    }

    @Deprecated
    @Test
    public void thrownError() {

        final ErrorResponse response = new ErrorResponse(new IOException("ahem"), mock(HttpHeaders.class), mock(UriInfo.class), true, false);
        assertNotNull(response.getStackTrace());
        assertNull(response.getCause());
    }

    @Test
    public void thrownErrorWithMDC() {

        MDC.put(MDCKeys.REQUEST_ID, "abc");
        MDC.put(MDCKeys.HOST, "localhost");
        MDC.put(MDCKeys.REQUEST_URI, "http://hello");
        MDC.put(MDCKeys.JWT_ID, "def");
        final ErrorResponse response = new ErrorResponse(new IOException("ahem"), mock(UriInfo.class), true);
        assertNotNull(response.getStackTrace());
        assertNull(response.getCause());
        assertEquals(URI.create("http://hello"), response.getRequestUri());
        assertEquals("abc", response.getRequestId());
        assertEquals("def", response.getJwtId());
        assertEquals("localhost", response.getHost());

    }

}
