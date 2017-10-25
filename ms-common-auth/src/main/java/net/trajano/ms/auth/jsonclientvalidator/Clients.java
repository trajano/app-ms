package net.trajano.ms.auth.jsonclientvalidator;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds a list of client info.
 *
 * @author Archimedes Trajano
 */
@XmlRootElement
public class Clients {

    @XmlElement(name = "clients",
        required = true,
        type = ClientInfo.class)
    private List<ClientInfo> clientInfoList;

    public List<ClientInfo> getClients() {

        return clientInfoList;
    }

    public void setClients(final List<ClientInfo> clients) {

        clientInfoList = clients;
    }
}
