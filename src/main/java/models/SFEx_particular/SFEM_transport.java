package models.SFEx_particular;

import models.base.SFEE;
import models.base.SFEI;
import models.base.SFEM;
import models.part_prodTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.TreeMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM_transport extends SFEM  {

    @XmlElement
    private SFEE sfeeTransport;

    public SFEM_transport() {
    }

    public SFEM_transport(String name) {
        super(name, SFEM_type.TRANSPORT);
    }
    public SFEE getSfeeTransport() {
        return sfeeTransport;
    }
    public void setSfeeTransport(SFEE sfeeTransport) {
        this.sfeeTransport = sfeeTransport;
    }



}
