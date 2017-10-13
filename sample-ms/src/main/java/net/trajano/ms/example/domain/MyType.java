package net.trajano.ms.example.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MyType {

    private String bar;

    private String foo;

    public String getBar() {

        return bar;
    }

    public String getFoo() {

        return foo;
    }

    public void setBar(final String bar) {

        this.bar = bar;
    }

    public void setFoo(final String foo) {

        this.foo = foo;
    }
}
