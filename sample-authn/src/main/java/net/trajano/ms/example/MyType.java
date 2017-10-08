package net.trajano.ms.example;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MyType {

    private String foo;

    public String getFoo() {

        return foo;
    }

    public void setFoo(final String foo) {

        this.foo = foo;
    }
}
