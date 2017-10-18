package net.trajano.ms.example.authz.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.trajano.ms.common.oauth.ClientValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Collectors;

@Component
public class SampleClientValidator implements
    ClientValidator {

    private Clients clients;

    @Override
    public URI getJwksUri(String clientId) {

        return getClientInfo(clientId).getJwksUri();
    }

    @Override
    public boolean isValid(String grantType,
        String clientId,
        String clientSecret) {

        ClientInfo clientInfo = getClientInfo(clientId);
        return clientInfo != null && clientInfo.matches(grantType, clientId, clientSecret);
    }

    private ClientInfo getClientInfo(String clientId) {

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

    @PostConstruct
    public void init() throws IOException {

        clients = objectMapper.readerFor(Clients.class).readValue(clientsFile);
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${clientsFile:clients.json}")
    private File clientsFile;

}
