package net.trajano.ms.swagger.internal;

import java.net.URI;
import java.util.Collections;

import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;

@JsonInclude(Include.NON_NULL)
public class ClonableSwagger extends Swagger {

    /**
     * Creates a shallow copy of the swagger but set the schemes, host and base
     * path based on {@link UriInfo}.
     *
     * @param uriInfo
     *            URI info of the request.
     * @return shallow copy
     */
    public ClonableSwagger withUriInfo(final UriInfo uriInfo) {

        final ClonableSwagger copy = new ClonableSwagger();
        copy.swagger = swagger;
        copy.info = info;
        final URI requestUri = uriInfo.getRequestUri();
        if (requestUri.getPort() != -1) {
            copy.host = requestUri.getHost() + ":" + requestUri.getPort();
        } else {
            copy.host = requestUri.getHost();
        }
        copy.basePath = basePath;
        copy.tags = tags;

        copy.schemes = Collections.singletonList(Scheme.forValue(requestUri.getScheme()));

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
