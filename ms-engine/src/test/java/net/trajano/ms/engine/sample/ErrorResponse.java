package net.trajano.ms.engine.sample;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlRootElement
@XmlType(propOrder = {
    "error",
    "errorDescription",
    "errorClass",
    "requestId",
    "threadId",
    "stackTrace",
    "cause"
})
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

    @XmlRootElement
    @SuppressWarnings("unused")
    private static class LocalStackTraceElement {

        @XmlElement(name = "class")
        private final String className;

        @XmlElement(name = "file")
        private final String fileName;

        @XmlElement(name = "line")
        private final int lineNumber;

        @XmlElement(name = "method")
        private final String methodName;

        protected LocalStackTraceElement() {

            className = null;
            fileName = null;
            lineNumber = -1;
            methodName = null;
        }

        public LocalStackTraceElement(final StackTraceElement ste) {

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

    @XmlElement(name = "error_class")
    private final String errorClass;

    /**
     * The error description. This follows OAuth 2.0 error responses.
     */
    @XmlElement(name = "error_description")
    private final String errorDescription;

    /**
     * The request ID.
     */
    @XmlElement(name = "request_id")
    private final String requestId;

    /**
     * The stack trace.
     */
    @XmlElement(name = "stack_trace")
    private final List<LocalStackTraceElement> stackTrace = new LinkedList<>();

    @XmlElement(name = "thread_id")
    private final String threadId;

    /**
     * Constructs an empty ErrorResponse. This is used for {@link #cause}.
     */
    protected ErrorResponse() {

        cause = null;
        error = null;
        errorClass = null;
        errorDescription = null;
        requestId = null;
        threadId = null;
    }

    public ErrorResponse(final Throwable e) {

        error = "server_error";
        errorDescription = e.getLocalizedMessage();
        errorClass = e.getClass().getName();
        threadId = Thread.currentThread().getName();
        requestId = null;
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

    public List<LocalStackTraceElement> getStackTrace() {

        return stackTrace;
    }

    public String getThreadId() {

        return threadId;
    }

    private boolean isInternalClass(final String className) {

        return className.startsWith("javax.") || className.startsWith("java.") || className.startsWith("sun.");
    }

}
