package utils.serialize;

import controllers.production.cSFEM_production;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

@XmlRootElement(name = "ProductionControllers")
public class production implements Externalizable {

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(C_Production);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.C_Production = new ArrayList<>((ArrayList<cSFEM_production>) in.readObject());
    }

    private ArrayList<cSFEM_production> C_Production = new ArrayList<>();

    public production() {
    }

    public production(ArrayList<cSFEM_production> c_Production) {
        C_Production = c_Production;
    }

    @XmlElement(name = "C_Production")
    public ArrayList<cSFEM_production> getC_Production() {
        return C_Production;
    }


}
