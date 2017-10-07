package net.trajano.ms.engine.internal.swagger;

import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.vertx.ext.web.RoutingContext;

@JsonInclude(Include.NON_NULL)
public class ClonableSwagger extends Swagger {

    /**
     * Creates a shallow copy of the swagger but set the schemes, host and base
     * path based on {@link RoutingContext}.
     *
     * @param baseUri
     *            base URI
     * @return shallow copy
     */
    public ClonableSwagger withRoutingContext(final RoutingContext context) {

        final ClonableSwagger copy = new ClonableSwagger();
        copy.swagger = swagger;
        copy.info = info;
        copy.host = context.request().host();
        copy.basePath = context.currentRoute().getPath();
        copy.tags = tags;

        copy.schemes = Collections.singletonList(Scheme.forValue(context.request().scheme()));

        copy.consumes = consumes;
        copy.produces = produces;
        copy.security = security;
        copy.paths = paths;
        copy.securityDefinitions = securityDefinitions;
        copy.definitions = definitions;
        copy.parameters = parameters;
        copy.responses = responses;
        copy.externalDocs = externalDocs;
        copy.vendorExtensions = vendorExtensions;
        return copy;
    }

}
