package net.trajano.ms.authz.jsonclientvalidator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.trajano.ms.authz.spi.ClientValidator;
import net.trajano.ms.core.JsonOps;

/**
 * A client validator that uses a JSON file for its data.
 *
 * @author Archimedes Trajano
 */
@Component
public class JsonClientValidator implements
    ClientValidator {

    private Clients clients;

    @Value("${client_validator.file:clients.json}")
    private File clientsFile;

    @Inject
    private JsonOps jsonOps;

    /**
     * Flag to check if Origin should be checked.
     */
    @Value("${client_validator.require_origin_check:true}")
    private boolean requireOriginCheck;

    private ClientInfo getClientInfo(final String clientId) {

        return clients.getClients().stream()
            .filter(info -> clientId.equals(info.getClientId())).collect(
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        if (list.isEmpty()) {
                            return null;
                        } else if (list.size() > 1) {
                            throw new IllegalStateException("more than one match found");
                        }
                        return list.get(0);
                    }));
    }

    @Override
    public URI getJwksUri(final String clientId) {

        return getClientInfo(clientId).getJwksUri();
    }

    @Override
    public URI getRedirectUri(final String clientId) {

        return getClientInfo(clientId).getRedirectUri();
    }

    @PostConstruct
    public void init() throws IOException {

        clients = jsonOps.fromJson(
            new FileReader(clientsFile), Clients.class);
    }

    @Override
    public boolean isOriginAllowed(final String clientId,
        final URI origin) {

        if (requireOriginCheck) {
            return getClientInfo(clientId).isOriginAllowed(origin);
        } else {
            return true;
        }
    }

    @Override
    public boolean isOriginAllowed(final URI origin) {

        if (requireOriginCheck) {
            return clients.getClients().stream()
                .anyMatch(info -> info.getOrigin().equals(origin));
        } else {
            return true;
        }
    }

    @Override
    public boolean isValid(final String grantType,
        final String clientId,
        final String clientSecret) {

        final ClientInfo clientInfo = getClientInfo(clientId);
        if (grantType == null) {
            return clientInfo != null && clientInfo.matches(clientId, clientSecret);
        } else {
            return clientInfo != null && clientInfo.matches(grantType, clientId, clientSecret);
        }
    }

}
