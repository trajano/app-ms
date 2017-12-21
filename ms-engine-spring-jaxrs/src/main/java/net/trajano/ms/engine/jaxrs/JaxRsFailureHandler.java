package net.trajano.ms.engine.jaxrs;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class JaxRsFailureHandler implements
    Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsFailureHandler.class);

    private static void sendErrorResponse(final RoutingContext context,
        final WebApplicationException webAppException) {

        context.response().setStatusCode(webAppException.getResponse().getStatus());
        context.response().setStatusMessage(webAppException.getResponse().getStatusInfo().getReasonPhrase());
        if (context.request().method() != HttpMethod.HEAD) {
            if (webAppException.getResponse().getMediaType() == null) {
                context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
            } else {
                context.response().putHeader(HttpHeaders.CONTENT_TYPE, webAppException.getResponse().getMediaType().toString());
            }
            context.response().end(webAppException.getResponse().getStatusInfo().getReasonPhrase());
        }
    }

    @Override
    public void handle(final RoutingContext context) {

        final Throwable wae = context.failure();
        if (wae instanceof ClientErrorException) {
            // Use a lower level of logging when it is a client error exception
            final WebApplicationException webAppException = (WebApplicationException) wae;
            LOG.debug(wae.getMessage(), wae);
            sendErrorResponse(context, webAppException);
        } else if (wae instanceof WebApplicationException) {
            final WebApplicationException webAppException = (WebApplicationException) wae;
            LOG.error(wae.getMessage(), wae);
            sendErrorResponse(context, webAppException);
        } else {
            LOG.error(wae.getMessage(), wae);
            context.response().setStatusCode(500);
            context.response().setStatusMessage(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            if (context.request().method() != HttpMethod.HEAD) {
                context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
                context.response().end(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            }
        }

    }

}
