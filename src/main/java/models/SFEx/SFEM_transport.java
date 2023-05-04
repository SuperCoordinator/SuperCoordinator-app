package models.SFEx;

import models.base.SFEE;
import models.base.SFEM;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM_transport extends SFEM  {

    @XmlElement
    private SFEE sfeeTransport;

    public SFEM_transport() {
    }

    public SFEM_transport(String name) {
        super(name, SFEM_role.TRANSPORT);
    }
    public SFEE getSfeeTransport() {
        return sfeeTransport;
    }
    public void setSfeeTransport(SFEE sfeeTransport) {
        this.sfeeTransport = sfeeTransport;
    }



}
