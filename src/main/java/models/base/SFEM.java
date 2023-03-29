package models.base;

import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_transport;
import models.part_prodTime;

import javax.xml.bind.annotation.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.TreeMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
//@XmlSeeAlso({SFEM_production.class, SFEM_transport.class})
public class SFEM implements Externalizable {
    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sfemType);
        out.writeObject(name);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.sfemType = (SFEM_type) in.readObject();
        this.name = (String) in.readObject();

    }

    public enum SFEM_type {
        PRODUCTION,
        TRANSPORT
    }
    @XmlAttribute
    private SFEM_type sfemType;
    @XmlAttribute
    private String name;
    private TreeMap<Integer, part_prodTime> productionHistory = new TreeMap<>();

    public SFEM() {
    }

    public SFEM(String name, SFEM_type sfemType) {
        this.sfemType = sfemType;
        this.name = name;
    }

//    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

//    @XmlAttribute(name = "type")
    public SFEM_type getSfemType() {
        return sfemType;
    }

    public void addPartToProductionHistory(part_prodTime producedPart) {
        productionHistory.put(producedPart.part().getId(), producedPart);
    }

//    @XmlTransient
    public TreeMap<Integer, part_prodTime> getProductionHistory() {
        return productionHistory;
    }
}
