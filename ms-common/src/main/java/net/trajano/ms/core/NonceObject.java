package net.trajano.ms.core;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NonceObject {

    @XmlElement(name = "nonce",
        required = true)
    private String nonce;

    public String getNonce() {

        return nonce;
    }

    public void setNonce(final String nonce) {

        this.nonce = nonce;
    }

}
