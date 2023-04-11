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
public class SFEM {

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

    public String getName() {
        return name;
    }

    public SFEM_type getSfemType() {
        return sfemType;
    }

    public void addPartToProductionHistory(part_prodTime producedPart) {
        productionHistory.put(producedPart.part().getId(), producedPart);
    }

    public TreeMap<Integer, part_prodTime> getProductionHistory() {
        return productionHistory;
    }
}
