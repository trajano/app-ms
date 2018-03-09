package net.trajano.ms.example.beans;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Example with BeanValidation.
 */
@XmlRootElement
public class ContactCard {

    @XmlElement(name = "children")
    @Valid
    private final List<ContactCard> children = new ArrayList<>();

    private String fullName;

    @DecimalMin(value = "1")
    @NotNull
    private Long id;

    private String phone;

    @NotNull(message = "{contact.wrong.name}")
    public String getFullName() {

        return fullName;
    }

    public Long getId() {

        return id;
    }

    @Pattern(message = "{contact.wrong.phone}",
        regexp = "[0-9]{3,9}")
    public String getPhone() {

        return phone;
    }

    public void setFullName(final String fullName) {

        this.fullName = fullName;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public void setPhone(final String phone) {

        this.phone = phone;
    }
}
