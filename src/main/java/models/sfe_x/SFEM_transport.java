package models.sfe_x;

import models.base.SFEE;
import models.base.SFEM;
import models.base.SFE_role;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM_transport extends SFEM {

    public enum configuration {
        SFEI2SFEI,
        WH2SFEI,
        SFEI2WH,
        SFEI2RealSFEI,
        RealSFEI2SFEI,
        WH2RealSFEI,
        RealSFEI2WH

    }

    @XmlElement
    private SFEE sfeeTransport;

    @XmlElement
    private configuration transport_configuration;

    public SFEM_transport() {
    }

    public SFEM_transport(String name, configuration transport_configuration) {
        super(name, SFE_role.TRANSPORT);
        this.transport_configuration = transport_configuration;
    }

    public SFEE getSfeeTransport() {
        return sfeeTransport;
    }

    public void setSfeeTransport(SFEE sfeeTransport) {
        this.sfeeTransport = sfeeTransport;
    }

    public configuration getTransport_configuration() {
        return transport_configuration;
    }
}
