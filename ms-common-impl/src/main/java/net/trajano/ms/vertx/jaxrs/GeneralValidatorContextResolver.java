package net.trajano.ms.vertx.jaxrs;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.validation.GeneralValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Provider
@Produces(MediaType.WILDCARD)
public class GeneralValidatorContextResolver implements
    ContextResolver<GeneralValidator> {

    @Autowired
    private GeneralValidator generalValidator;

    @Override
    public GeneralValidator getContext(final Class<?> type) {

        return generalValidator;

    }

}
