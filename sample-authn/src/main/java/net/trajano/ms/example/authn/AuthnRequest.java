package net.trajano.ms.example.authn;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthnRequest {

    @XmlElement(name = "grant_type")
    private String grantType;

    @XmlElement(name = "password")
    private String password;

    @XmlElement(name = "username")
    private String username;

    public String getGrantType() {

        return grantType;
    }

    public String getPassword() {

        return password;
    }

    public String getUsername() {

        return username;
    }

    public void setGrantType(final String grantType) {

        this.grantType = grantType;
    }

    public void setPassword(final String password) {

        this.password = password;
    }

    public void setUsername(final String username) {

        this.username = username;
    }
}
