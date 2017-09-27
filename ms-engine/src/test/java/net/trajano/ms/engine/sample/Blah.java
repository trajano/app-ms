package net.trajano.ms.engine.sample;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Blah {

    private String hello = "mew";

    public String getHello() {

        return hello;
    }

    public void setHello(final String hello) {

        this.hello = hello;
    }
}
