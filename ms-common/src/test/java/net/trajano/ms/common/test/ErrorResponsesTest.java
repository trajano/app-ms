package net.trajano.ms.common.test;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import net.trajano.commons.testing.UtilityClassTestUtil;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.core.ErrorResponses;
import net.trajano.ms.core.Qualifiers;

public class ErrorResponsesTest {

    @Test
    public void coverUtilityClasses() throws Exception {

        UtilityClassTestUtil.assertUtilityClassWellDefined(ErrorCodes.class);
        UtilityClassTestUtil.assertUtilityClassWellDefined(ErrorResponses.class);
        UtilityClassTestUtil.assertUtilityClassWellDefined(Qualifiers.class);
    }

    @Test
    public void jaxrsBadRequesException() {

        assertEquals(400, new BadRequestException().getResponse().getStatus());
    }

    @Test
    public void testBadRequestError() {

        final WebApplicationException e = ErrorResponses.badRequest("bad", "bad");
        assertEquals(400, e.getResponse().getStatus());
    }

    @Test
    public void testInternalServerError() {

        final InternalServerErrorException internalServerErrorException = ErrorResponses.internalServerError("bad");
        assertEquals(500, internalServerErrorException.getResponse().getStatus());
    }

    @Test
    public void testInvalidRequestError() {

        final WebApplicationException e = ErrorResponses.invalidRequest("bad");
        assertEquals(400, e.getResponse().getStatus());
    }

    @Test
    public void testUnauthorizedError() {

        final WebApplicationException e = ErrorResponses.unauthorized("bad", "bad2", "Bearer");
        assertEquals(401, e.getResponse().getStatus());
        assertEquals("Bearer", e.getResponse().getHeaderString("WWW-Authenticate"));
    }

}
