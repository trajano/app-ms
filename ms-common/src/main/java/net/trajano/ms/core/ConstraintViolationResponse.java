package net.trajano.ms.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

/**
 * Contraint violation response. This is the error response that is sent when
 * there is a Bean Validation error on the input data.
 *
 * @author Archimedes Trajano
 */
@XmlRootElement
@XmlType(propOrder = {
    "error",
    "errorDescription",
    "errorClass",
    "violations",
    "requestId",
    "requestUri",
    "threadId",
    "host",
    "jwtId",
    "stackTrace",
    "cause"
})
@JsonInclude(Include.NON_NULL)
public class ConstraintViolationResponse extends ErrorResponse {

    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(propOrder = {
        "message",
        "messageTemplate",
        "path"
    })
    @SuppressWarnings("unused")
    private static class ConstraintViolationElement {

        /**
         * Prefix added to the path.
         */
        private final String pathPrefix;

        /**
         * The wrapped violation.
         */
        private final ConstraintViolation<?> violation;

        ConstraintViolationElement(final ConstraintViolation<?> violation) {

            this.violation = violation;
            pathPrefix = "";
        }

        ConstraintViolationElement(final ConstraintViolation<?> violation,
            final String pathPrefix) {

            this.violation = violation;
            this.pathPrefix = pathPrefix + ".";
        }

        public String getMessage() {

            return violation.getMessage();
        }

        public String getPath() {

            return pathPrefix + violation.getPropertyPath().toString();
        }

        public String getTemplate() {

            return violation.getMessageTemplate();
        }
    }

    /**
     * The request ID. This is obtained from the header.
     */
    @ApiModelProperty(name = "violations",
        value = "Constraint violations.")
    @XmlElement(name = "violations")
    private final List<ConstraintViolationElement> violations;

    /**
     * Wraps a {@link Throwable} in an {@link ErrorResponse} with full stack trace
     * and cause if requested.
     *
     * @param e
     *            constraint violation exception to wrap
     * @param uriInfo
     *            URI info
     * @param showStackTrace
     *            flag to determine whether the stack trace is to be shown.
     */
    public ConstraintViolationResponse(final ConstraintViolationException e,
        final UriInfo uriInfo,
        final boolean showStackTrace) {

        super(e, uriInfo, showStackTrace);
        violations = StreamSupport.stream(e.getConstraintViolations().spliterator(), false).map(v -> new ConstraintViolationElement(v)).collect(Collectors.toList());

    }

    public ConstraintViolationResponse(final Map<String, Set<ConstraintViolation<Object>>> violationMap) {

        super(ErrorCodes.INVALID_REQUEST, "constraints violated");
        violations = new LinkedList<>();
        violationMap.forEach((key,
            violationSet) -> violationSet.forEach(v -> violations.add(new ConstraintViolationElement(v, key))));
    }
}
