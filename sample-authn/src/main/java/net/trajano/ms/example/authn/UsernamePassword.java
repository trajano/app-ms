package net.trajano.ms.example.authn;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UsernamePassword {

    private String password;

    private String username;

    public String getPassword() {

        return password;
    }

    public String getUsername() {

        return username;
    }

    public void setPassword(final String password) {

        this.password = password;
    }

    public void setUsername(final String username) {

        this.username = username;
    }
}
