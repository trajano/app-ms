package net.trajano.ms.gateway.handlers;

import static io.vertx.core.http.HttpHeaders.DATE;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static net.trajano.ms.gateway.providers.RequestIDProvider.REQUEST_ID;

import java.util.Map;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.trajano.ms.gateway.internal.Conversions;
import net.trajano.ms.gateway.internal.PathContext;
import net.trajano.ms.gateway.internal.Predicates;

@Component
@Order(SelfRegisteringRoutingContextHandler.CORE_PATHS + 6)
public class UnprotectedHandler extends SelfRegisteringRoutingContextHandler {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UnprotectedHandler.class);

    @Autowired
    private HttpClient httpClient;

    @Override
    public void handle(final RoutingContext context) {

        // if null or it is marked as protected then go on.
        final PathContext pathContext = PathContext.get(context);
        if (pathContext == null) {
            context.next();
            return;
        }

        LOG.debug("Handling {} with from={} to={} protected={} ended={}", context, pathContext.getFrom(), pathContext.getTo(), pathContext.isProtected(), context.request().isEnded());

        final HttpServerRequest contextRequest = context.request();

        contextRequest.setExpectMultipart(context.parsedHeaders().contentType().isPermitted() && "multipart".equals(context.parsedHeaders().contentType().component()));
        final RequestOptions clientRequestOptions = Conversions.toRequestOptions(pathContext.getTo(), contextRequest.uri().substring(pathContext.getFrom().length()));
        final HttpClientRequest clientRequest = httpClient.request(contextRequest.method(), clientRequestOptions, clientResponse -> {
            contextRequest.response().setChunked(clientResponse.getHeader(HttpHeaders.CONTENT_LENGTH) == null)
                .setStatusCode(clientResponse.statusCode());
            clientResponse.headers().forEach(e -> contextRequest.response().putHeader(e.getKey(), e.getValue()));
            clientResponse.handler(contextRequest.response()::write)
                .endHandler(v -> contextRequest.response().end());
        }).exceptionHandler(context::fail)
            .setChunked(true);

        StreamSupport.stream(contextRequest.headers().spliterator(), false)
            .filter(Predicates.HEADER_FORWARDABLE)
            .forEach(e -> clientRequest.putHeader(e.getKey(), e.getValue()));

        clientRequest.putHeader(REQUEST_ID, (String) context.get(REQUEST_ID));
        clientRequest.putHeader(DATE, RFC_1123_DATE_TIME.format(now(UTC)));
        final Map<String, String> additionalHeaders = context.get("additional_headers");
        if (additionalHeaders != null) {
            additionalHeaders.forEach(clientRequest::putHeader);
        }
        contextRequest.handler(clientRequest::write)
            .endHandler(v -> clientRequest.end());
        contextRequest.resume();

    }

    @Override
    public void register(final Router router) {

        router.get().handler(this);
        router.post().handler(this);
        router.put().handler(this);
        router.delete().handler(this);
    }
}
