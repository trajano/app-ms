package net.trajano.ms.vertx.test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import net.trajano.commons.testing.UtilityClassTestUtil;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.core.ErrorResponses;
import net.trajano.ms.core.Qualifiers;
import net.trajano.ms.vertx.jaxrs.JsonExceptionMapper;

public class ExceptionMapperTest {

    private JsonExceptionMapper mapper;

    @Test
    public void coverSetDebugFlags() {

        mapper.setDebugFlags();
    }

    @Test
    public void coverUtilityClasses() throws Exception {

        UtilityClassTestUtil.assertUtilityClassWellDefined(ErrorCodes.class);
        UtilityClassTestUtil.assertUtilityClassWellDefined(ErrorResponses.class);
        UtilityClassTestUtil.assertUtilityClassWellDefined(Qualifiers.class);
    }

    @Before
    public void setupMapper() {

        mapper = new JsonExceptionMapper();
        mapper.setDebugFlags();
        final HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(headers.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.WILDCARD_TYPE));
        mapper.setContextData(headers, Mockito.mock(UriInfo.class), true);
    }

    @Test
    public void testBadRequest() {

        final Response response = mapper.toResponse(new BadRequestException());
        Assert.assertEquals(400, response.getStatus());
    }

    @Test
    public void testBadRequestWithBody() {

        final Response resp = Response.status(400).entity("pooh").build();
        final Response response = mapper.toResponse(new BadRequestException(resp));
        Assert.assertEquals(400, response.getStatus());
    }

    @Test
    public void testChecked() {

        final Response response = mapper.toResponse(new IOException());
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void testCheckedHtml() {

        final HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(headers.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.TEXT_HTML_TYPE));
        mapper.setContextData(headers, Mockito.mock(UriInfo.class), true);

        final Response response = mapper.toResponse(new IOException("ahem"));
        Assert.assertEquals(500, response.getStatus());
        Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
        Assert.assertEquals("ahem", response.getEntity());
    }

    @Test
    public void testCheckedNested() {

        final Response response = mapper.toResponse(new IOException("blah", new GeneralSecurityException()));
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void testCheckedPlainText() {

        final HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(headers.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.TEXT_PLAIN_TYPE));
        mapper.setContextData(headers, Mockito.mock(UriInfo.class), true);

        final Response response = mapper.toResponse(new IOException("ahem"));
        Assert.assertEquals(500, response.getStatus());
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
        Assert.assertEquals("ahem", response.getEntity());
    }

    @Test
    public void testCheckedUnsupportedType() {

        final HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(headers.getAcceptableMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM_TYPE));
        mapper.setContextData(headers, Mockito.mock(UriInfo.class), true);

        final Response response = mapper.toResponse(new IOException("ahem"));
        Assert.assertEquals(500, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    public void testNotFound() {

        final Response response = mapper.toResponse(new NotFoundException());
        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testUnchecked() {

        final Response response = mapper.toResponse(new RuntimeException());
        Assert.assertEquals(500, response.getStatus());
    }
}
