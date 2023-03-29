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
//@XmlRootElement(name = "SFEM_transport")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM_transport extends SFEM implements Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(sfeeTransport);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.sfeeTransport = (SFEE) in.readObject();
    }

    @XmlElement
    private SFEE sfeeTransport;

    public SFEM_transport() {
    }

    public SFEM_transport(String name) {
        super(name, SFEM_type.TRANSPORT);
    }
//    @XmlElement(name = "SFEE_transport")
    public SFEE getSfeeTransport() {
        return sfeeTransport;
    }
    public void setSfeeTransport(SFEE sfeeTransport) {
        this.sfeeTransport = sfeeTransport;
    }



}
