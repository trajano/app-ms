package net.trajano.ms.core;

import static net.trajano.ms.spi.MDCKeys.REQUEST_ID;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;
import net.trajano.ms.spi.MDCKeys;

/**
 * Error response. This is the error response that gets built by the system for
 * any errors that occur.
 *
 * @author Archimedes Trajano
 */
@XmlRootElement
@XmlType(propOrder = {
    "error",
    "errorDescription",
    "errorClass",
    "requestId",
    "requestUri",
    "threadId",
    "host",
    "jwtId",
    "stackTrace",
    "cause"
})
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

    /**
     * Local stack trace element.
     */
    @XmlRootElement
    @SuppressWarnings("unused")
    private static class LocalStackTraceElement {

        /**
         * Class name.
         */
        @XmlElement(name = "class")
        private final String className;

        /**
         * File name.
         */
        @XmlElement(name = "file")
        private final String fileName;

        /**
         * Line number.
         */
        @XmlElement(name = "line")
        private final int lineNumber;

        /**
         * Method name.
         */
        @XmlElement(name = "method")
        private final String methodName;

        protected LocalStackTraceElement() {

            className = null;
            fileName = null;
            lineNumber = -1;
            methodName = null;
        }

        LocalStackTraceElement(final StackTraceElement ste) {

            className = ste.getClassName();
            methodName = ste.getMethodName();
            fileName = ste.getFileName();
            lineNumber = ste.getLineNumber();

        }

        public String getClassName() {

            return className;
        }

        public String getFileName() {

            return fileName;
        }

        public int getLineNumber() {

            return lineNumber;
        }

        public String getMethodName() {

            return methodName;
        }

    }

    /**
     * The cause of this {@link ErrorResponse} or <code>null</code> if the cause is
     * nonexistent or unknown.
     */
    @XmlElement(name = "cause",
        required = false)
    private final ErrorResponse cause;

    /**
     * The error code. This follows OAuth 2.0 error responses.
     */
    @XmlElement(required = true)
    private final String error;

    /**
     * Class name of the error.
     */
    @XmlElement(name = "error_class")
    private final String errorClass;

    /**
     * The error description. This follows OAuth 2.0 error responses.
     */
    @XmlElement(name = "error_description")
    private final String errorDescription;

    /**
     * Host of the server that threw the error. This shows the hostname and port.
     */
    @XmlElement(name = "host")
    private final String host;

    /**
     * JWT ID. This represents the session.
     */
    @XmlElement(name = "jwt_id")
    private final String jwtId;

    /**
     * The request ID. This is obtained from the header.
     */
    @ApiModelProperty(name = "request_id",
        value = "Request ID used to track the events for the request.")
    @XmlElement(name = "request_id")
    private final String requestId;

    /**
     * Request URI.
     */
    @XmlElement(name = "request_uri")
    private final URI requestUri;

    /**
     * The stack trace.
     */
    @ApiModelProperty(name = "stack_trace",
        value = "A list of stack trace elements.")
    @XmlElement(name = "stack_trace",
        type = LocalStackTraceElement.class)
    private final List<LocalStackTraceElement> stackTrace;

    /**
     * The thread ID.
     */
    @XmlElement(name = "thread_id")
    private final String threadId;

    /**
     * Constructs an empty ErrorResponse. This is used for {@link #cause} to prevent
     * repeating the same information.
     */
    protected ErrorResponse() {

        cause = null;
        error = null;
        errorClass = null;
        errorDescription = null;
        stackTrace = null;
        host = null;
        jwtId = null;
        threadId = null;
        requestId = null;
        requestUri = null;
    }

    /**
     * Creates an error response with just the error code and description. The
     * request ID will be taken from the MDC.
     *
     * @param error
     *            error code
     * @param errorDescription
     *            error description
     */
    public ErrorResponse(final String error,
        final String errorDescription) {

        this.error = error;
        this.errorDescription = errorDescription;
        requestId = MDC.get(REQUEST_ID);

        cause = null;
        errorClass = null;
        host = MDC.get(MDCKeys.HOST);
        jwtId = MDC.get(MDCKeys.JWT_ID);
        stackTrace = null;
        threadId = Thread.currentThread().getName();
        requestUri = calculateRequestUri();
    }

    /**
     * Creates an error response with just the error code and description. The
     * request ID will be taken from the MDC.
     *
     * @param error
     *            error code
     * @param errorDescription
     *            error description
     * @param requestId
     *            ignored
     * @deprecated the requestId value is ignored and is taken from the MDC.
     */
    @Deprecated
    public ErrorResponse(final String error,
        final String errorDescription,
        final String requestId) {

        this(error, errorDescription);
    }

    /**
     * Constructs ErrorResponse to chain the cause.
     *
     * @param e
     *            cause
     */
    protected ErrorResponse(final Throwable e) {

        if (e.getCause() != null) {
            cause = new ErrorResponse(e.getCause());
        } else {
            cause = null;
        }
        stackTrace = new LinkedList<>();
        for (final StackTraceElement ste : e.getStackTrace()) {
            if (!isInternalClass(ste.getClassName())) {
                stackTrace.add(new LocalStackTraceElement(ste));
            }
        }
        error = null;
        host = null;
        jwtId = null;
        errorClass = e.getClass().getName();
        errorDescription = e.getMessage();
        threadId = null;
        requestId = null;
        requestUri = null;
    }

    /**
     * Wraps a {@link Throwable} in an {@link ErrorResponse} with full stack trace
     * and cause if requested.
     *
     * @param e
     *            exception to wrap
     * @param headers
     *            ignored
     * @param uriInfo
     *            URI info
     * @param showStackTrace
     *            flag to determine whether the stack trace is to be shown.
     * @param showRequestUri
     *            ignored
     * @deprecated use {@link #ErrorResponse(Throwable, UriInfo, boolean)}
     */
    @Deprecated
    public ErrorResponse(final Throwable e,
        final HttpHeaders headers,
        final UriInfo uriInfo,
        final boolean showStackTrace,
        final boolean showRequestUri) {

        this(e, uriInfo, showStackTrace);
    }

    /**
     * Wraps a {@link Throwable} in an {@link ErrorResponse} with full stack trace
     * and cause if requested.
     *
     * @param e
     *            exception to wrap
     * @param uriInfo
     *            URI info
     * @param showStackTrace
     *            flag to determine whether the stack trace is to be shown.
     */
    public ErrorResponse(final Throwable e,
        final UriInfo uriInfo,
        final boolean showStackTrace) {

        error = ErrorCodes.SERVER_ERROR;
        errorDescription = e.getLocalizedMessage();
        errorClass = e.getClass().getName();
        threadId = Thread.currentThread().getName();
        if (showStackTrace) {
            stackTrace = new LinkedList<>();
            for (final StackTraceElement ste : e.getStackTrace()) {
                if (!isInternalClass(ste.getClassName())) {
                    stackTrace.add(new LocalStackTraceElement(ste));
                }
            }
            if (e.getCause() != null) {
                cause = new ErrorResponse(e.getCause());
            } else {
                cause = null;
            }
        } else {
            stackTrace = null;
            cause = null;
        }
        requestId = MDC.get(REQUEST_ID);
        host = MDC.get(MDCKeys.HOST);
        requestUri = calculateRequestUri();
        jwtId = MDC.get(MDCKeys.JWT_ID);

    }

    /**
     * Performs a null-check on the request URI data.
     *
     * @return request URI
     */
    private URI calculateRequestUri() {

        final String requestUriString = MDC.get(MDCKeys.REQUEST_URI);
        if (requestUriString == null) {

            return null;
        } else {
            return URI.create(requestUriString);
        }
    }

    public ErrorResponse getCause() {

        return cause;
    }

    public String getError() {

        return error;
    }

    public String getErrorClass() {

        return errorClass;
    }

    public String getErrorDescription() {

        return errorDescription;
    }

    public String getHost() {

        return host;
    }

    public String getJwtId() {

        return jwtId;
    }

    public String getRequestId() {

        return requestId;
    }

    public URI getRequestUri() {

        return requestUri;
    }

    public List<LocalStackTraceElement> getStackTrace() {

        return stackTrace;
    }

    public String getThreadId() {

        return threadId;
    }

    /**
     * Checks if it is an internal classes so the stack trace does not get too long.
     * Internal classes are "java.*", "javax.*", "sun.*"
     *
     * @param className
     *            class to check
     * @return <code>true</code> if it is an internal class.
     */
    private boolean isInternalClass(final String className) {

        return className.startsWith("javax.") || className.startsWith("java.") || className.startsWith("sun.");
    }

}
