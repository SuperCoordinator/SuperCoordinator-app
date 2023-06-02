package models.base;

import models.part_prodTime;

import javax.xml.bind.annotation.*;
import java.util.TreeMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM {

    @XmlAttribute
    private SFE_role sfemRole;
    @XmlAttribute
    private String name;
    private TreeMap<Integer, part_prodTime> productionHistory = new TreeMap<>();

    public SFEM() {
    }

    public SFEM(String name, SFE_role sfemType) {
        this.sfemRole = sfemType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SFE_role getSfemRole() {
        return sfemRole;
    }

    public void addPartToProductionHistory(part_prodTime producedPart) {
        productionHistory.put(producedPart.part().getId(), producedPart);
    }

    public TreeMap<Integer, part_prodTime> getProductionHistory() {
        return productionHistory;
    }
}
