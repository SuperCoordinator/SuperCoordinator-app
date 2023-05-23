package models.base;

import models.part_prodTime;

import javax.xml.bind.annotation.*;
import java.util.TreeMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM {

    public enum SFEM_role {
        PRODUCTION,
        TRANSPORT,
        WAREHOUSE
    }

    @XmlAttribute
    private SFEM_role sfemRole;
    @XmlAttribute
    private String name;
    private TreeMap<Integer, part_prodTime> productionHistory = new TreeMap<>();

    public SFEM() {
    }

    public SFEM(String name, SFEM_role sfemType) {
        this.sfemRole = sfemType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SFEM_role getSfemRole() {
        return sfemRole;
    }

    public void addPartToProductionHistory(part_prodTime producedPart) {
        productionHistory.put(producedPart.part().getId(), producedPart);
    }

    public TreeMap<Integer, part_prodTime> getProductionHistory() {
        return productionHistory;
    }
}
