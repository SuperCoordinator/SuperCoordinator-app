package utils.serialize;

import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class transport implements Externalizable {
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(C_Transport);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.C_Transport = new ArrayList<>((ArrayList<cSFEM_transport>) in.readObject());
    }

    private ArrayList<cSFEM_transport> C_Transport = new ArrayList<>();

    public transport() {
    }

    public transport(ArrayList<cSFEM_transport> c_Transport) {
        C_Transport = c_Transport;
    }

    @XmlElement
    public ArrayList<cSFEM_transport> getC_Transport() {
        return C_Transport;
    }


}
