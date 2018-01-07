package net.trajano.ms.gateway.handlers;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Errors;
import net.trajano.ms.gateway.internal.MediaTypes;

/**
 * This provides the error handling for those requests that have not been
 * handled correctly in the upstream routes.
 *
 * @author Archimedes Trajano
 */
@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_GLOBAL)
public class GlobalFailureHandler extends SelfRegisteringRoutingContextHandler {

    private static final MessageSourceResolvable GATEWAY_ERROR = new DefaultMessageSourceResolvable("err.gateway_error");

    private static final MessageSourceResolvable GATEWAY_TIMEOUT = new DefaultMessageSourceResolvable("err.gateway_timeout");

    private static final MessageSourceResolvable INTERNAL_SERVER_ERROR = new DefaultMessageSourceResolvable("err.internal_server_error");

    private static final Logger LOG = LoggerFactory.getLogger(GlobalFailureHandler.class);

    @Autowired
    private MessageSource r;

    @Override
    public void handle(final RoutingContext context) {

        if (LOG.isErrorEnabled()) {
            LOG.error("Unhandled server exception statusCode={} responseEnded={} uri={}", context.statusCode(), context.response().ended(), context.request().uri(), context.failure());
        }
        if (!context.response().ended()) {
            if (context.failure() instanceof ConnectException) {
                context.response().setStatusCode(504)
                    .setStatusMessage(r.getMessage(GATEWAY_TIMEOUT, Locale.getDefault()))
                    .putHeader(CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                    .end(Errors.serverError(r.getMessage(GATEWAY_TIMEOUT, Locale.getDefault())).toBuffer());
            } else if (context.failure() instanceof UnknownHostException) {
                context.response().setStatusCode(503)
                    .setStatusMessage(r.getMessage(GATEWAY_ERROR, Locale.getDefault()))
                    .putHeader(CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                    .end(Errors.serverError(r.getMessage(GATEWAY_ERROR, Locale.getDefault())).toBuffer());
            } else {
                context.response().setStatusCode(context.statusCode() == -1 ? 500 : context.statusCode())
                    .setStatusMessage(r.getMessage(INTERNAL_SERVER_ERROR, Locale.getDefault()))
                    .putHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                    .end(Errors.serverError(r.getMessage(INTERNAL_SERVER_ERROR, Locale.getDefault())).toBuffer());
            }
        }

    }

    @Override
    public void register(final Router router) {

        router.route().failureHandler(this);
    }
}
