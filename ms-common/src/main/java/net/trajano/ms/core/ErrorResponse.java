package net.trajano.ms.core;

import static net.trajano.ms.core.Qualifiers.REQUEST_ID;

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

        this(error, errorDescription, MDC.get(REQUEST_ID));
    }

    public ErrorResponse(final String error,
        final String errorDescription,
        final String requestId) {

        this.error = error;
        this.errorDescription = errorDescription;
        this.requestId = requestId;

        cause = null;
        errorClass = null;
        stackTrace = null;
        threadId = Thread.currentThread().getName();
        requestUri = null;
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
        errorClass = e.getClass().getName();
        errorDescription = e.getMessage();
        threadId = null;
        requestId = null;
        requestUri = null;
    }

    public ErrorResponse(final Throwable e,
        final HttpHeaders headers,
        final UriInfo uriInfo,
        final boolean showStackTrace,
        final boolean showRequestUri) {

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
        requestId = headers.getHeaderString(REQUEST_ID);
        requestUri = showRequestUri ? uriInfo.getRequestUri() : null;

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

    public String getRequestId() {

        return requestId;
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
