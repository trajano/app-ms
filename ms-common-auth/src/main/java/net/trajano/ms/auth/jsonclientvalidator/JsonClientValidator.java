package net.trajano.ms.auth.jsonclientvalidator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.trajano.ms.auth.spi.ClientValidator;
import net.trajano.ms.core.JsonOps;

@Component
public class JsonClientValidator implements
    ClientValidator {

    private Clients clients;

    @Value("${clientsFile:clients.json}")
    private File clientsFile;

    @Inject
    private JsonOps jsonOps;

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

    @PostConstruct
    public void init() throws IOException {

        clients = jsonOps.fromJson(
            new FileReader(clientsFile), Clients.class);
    }

    @Override
    public boolean isValid(final String grantType,
        final String clientId,
        final String clientSecret) {

        final ClientInfo clientInfo = getClientInfo(clientId);
        return clientInfo != null && clientInfo.matches(grantType, clientId, clientSecret);
    }

}
