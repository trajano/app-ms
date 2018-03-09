package net.trajano.ms.vertx.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.validation.GeneralValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.trajano.ms.core.ConstraintViolationResponse;
import net.trajano.ms.core.ErrorResponses;

/**
 * This performs validation checks on the request message. If the request
 * message does not pass validation it will throw a bad request exception.
 *
 * @author Archimedes Trajano
 */
@Component
public class BeanValidator implements
    GeneralValidator {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BeanValidator.class);

    @Autowired
    private Validator validator;

    @Override
    public void checkViolations(final HttpRequest request) {

        // does nothing

    }

    /**
     * Obtain the name from JAX-RS annotations. If the parameter is not annotated is
     * uses "body" as the name.
     *
     * @param parameter
     *            parameter
     * @return parameter name
     */
    private String getParameterName(final Parameter parameter) {

        final String name;
        if (parameter.getAnnotation(FormParam.class) != null) {
            name = "form." + parameter.getAnnotation(FormParam.class).value();
        } else if (parameter.getAnnotation(QueryParam.class) != null) {
            name = "query." + parameter.getAnnotation(QueryParam.class).value();
        } else if (parameter.getAnnotation(PathParam.class) != null) {
            name = "path." + parameter.getAnnotation(PathParam.class).value();
        } else if (parameter.getAnnotation(HeaderParam.class) != null) {
            name = "header." + parameter.getAnnotation(HeaderParam.class).value();
        } else if (parameter.isAnnotationPresent(Context.class)) {
            name = parameter.getType().getName();
        } else {
            name = "body";
        }
        return name;
    }

    @Override
    public boolean isMethodValidatable(final Method method) {

        return true;
    }

    @Override
    public boolean isValidatable(final Class<?> clazz) {

        return true;
    }

    @Override
    public void validate(final HttpRequest request,
        final Object object,
        final Class<?>... groups) {

        LOG.debug("validate {} {}", request, object);

    }

    @Override
    public void validateAllParameters(final HttpRequest request,
        final Object object,
        final Method method,
        final Object[] parameterValues,
        final Class<?>... groups) {

        LOG.debug("validateAllParameters {} {} {}", request, object, parameterValues);

        final Map<String, Set<ConstraintViolation<Object>>> violationMap = new LinkedHashMap<>();
        boolean hasViolation = false;
        for (int i = 0; i < method.getParameterCount(); ++i) {

            final Object value = parameterValues[i];
            final Parameter parameter = method.getParameters()[i];

            final String name = getParameterName(parameter);

            final Set<ConstraintViolation<Object>> violations;
            if (value == null) {
                if (parameter.isAnnotationPresent(NotNull.class)) {
                    throw ErrorResponses.invalidRequest("missing value for " + name);
                } else {
                    continue;
                }
            } else {
                violations = validator.validate(value);
            }
            if (violations.isEmpty()) {
                continue;
            }

            violationMap.put(name, violations);
            hasViolation = true;
        }
        if (hasViolation) {
            throw new BadRequestException(Response.status(Status.BAD_REQUEST).entity(new ConstraintViolationResponse(violationMap)).build());
        }

    }

    @Override
    public void validateReturnValue(final HttpRequest request,
        final Object object,
        final Method method,
        final Object returnValue,
        final Class<?>... groups) {

        LOG.debug("validateReturnValue  {} {} {}", request, object, returnValue);

        final Set<ConstraintViolation<Object>> errors = validator.validate(returnValue);

        if (!errors.isEmpty()) {
            throw new InternalServerErrorException("result object validation failed", new ConstraintViolationException(errors));
        }

    }

}
