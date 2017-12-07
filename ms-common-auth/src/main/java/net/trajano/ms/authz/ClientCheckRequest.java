package net.trajano.ms.authz;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@ApiModel(description = "Client Check Request")
public class ClientCheckRequest implements
    Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -6845634801856757737L;

    @ApiModelProperty(name = "authorization",
        value = "Client Authorization",
        notes = "This is the value that is passed from the client as is.  This is needed since the client check API requires the gateway to authenticate against it.",
        example = "Bearer abcd1234")
    @XmlElement(name = "authorization")
    private String authorization;

    @ApiModelProperty(name = "origin",
        value = "Origin header value",
        required = true)
    @XmlElement(name = "origin",
        required = true)
    private String origin;

    public String getAuthorization() {

        return authorization;
    }

    public String getOrigin() {

        return origin;
    }

    public void setAuthorization(final String authorization) {

        this.authorization = authorization;
    }

    public void setOrigin(final String origin) {

        this.origin = origin;
    }

}
