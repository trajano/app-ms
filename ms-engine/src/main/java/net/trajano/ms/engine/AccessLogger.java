package net.trajano.ms.engine;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

/**
 * This writes out log entries in Apache's combined log format
 *
 * <pre>
 * 127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326 "http://www.example.com/start.html" "Mozilla/4.08 [en] (Win98; I ;Nav)"
 * </pre>
 *
 * @author Archimedes Trajano
 */
public class AccessLogger {

    public static String buildLogLine(final HttpServerRequest req,
        final HttpServerResponse resp) {

        return buildLogLine(req, resp, "-");
    }

    public static String buildLogLine(final HttpServerRequest req,
        final HttpServerResponse resp,
        final String user) {

        final String timestamp = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss Z]", Locale.ENGLISH).format(Date.from(Instant.now()));
        final String protocol;
        if (req.isSSL()) {
            protocol = "HTTPS";
        } else {
            protocol = "HTTP";
        }
        final String referer;
        if (req.getHeader(HttpHeaders.REFERER) == null) {
            referer = "-";
        } else {
            referer = String.format("\"%s\"", req.getHeader(HttpHeaders.REFERER));
        }

        final String userAgent;
        if (req.getHeader(HttpHeaders.USER_AGENT) == null) {
            userAgent = "-";
        } else {
            userAgent = String.format("\"%s\"", req.getHeader(HttpHeaders.USER_AGENT));
        }

        return String.format("%s - %s %s \"%s %s %s\" %d %d %s %s",
            req.remoteAddress().host(),
            user,
            timestamp,
            req.method(),
            req.absoluteURI(),
            protocol,
            resp.getStatusCode(),
            resp.bytesWritten(),
            referer,
            userAgent);
    }

}
