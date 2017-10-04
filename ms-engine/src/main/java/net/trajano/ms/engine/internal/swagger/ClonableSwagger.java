package net.trajano.ms.engine.internal.swagger;

import java.net.URI;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.vertx.core.http.HttpServerRequest;

@JsonInclude(Include.NON_NULL)
public class ClonableSwagger extends Swagger {

    /**
     * Creates a shallow copy of the swagger but set the scheme, host and base
     * path to the base URI
     *
     * @param baseUri
     *            base URI
     * @return shallow copy
     */
    public ClonableSwagger withBaseUri(final URI baseUri) {

        final ClonableSwagger copy = new ClonableSwagger();
        copy.swagger = swagger;
        copy.info = info;
        if (baseUri.getPort() > 0) {
            copy.host = baseUri.getHost() + ":" + baseUri.getPort();
        } else {
            copy.host = baseUri.getHost();
        }
        copy.basePath = baseUri.getPath();
        copy.tags = tags;
        copy.schemes = Collections.singletonList(Scheme.forValue(baseUri.getScheme()));
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

    public ClonableSwagger withRequest(final HttpServerRequest request) {

        final ClonableSwagger copy = new ClonableSwagger();
        copy.swagger = swagger;
        copy.info = info;
        copy.host = request.host();
        copy.basePath = basePath;
        copy.tags = tags;

        if (request.isSSL()) {
            copy.schemes = Collections.singletonList(Scheme.HTTPS);
        } else {
            copy.schemes = Collections.singletonList(Scheme.HTTP);
        }
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
