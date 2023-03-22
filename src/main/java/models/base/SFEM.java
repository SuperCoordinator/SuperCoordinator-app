package models.base;

import models.part_prodTime;

import java.util.TreeMap;

public class SFEM {

    public enum SFEM_type {
        PRODUCTION,
        TRANSPORT
    }

    private final SFEM_type sfemType;
    private final String name;
    private final TreeMap<Integer, part_prodTime> productionHistory;

    public SFEM(String name, SFEM_type sfemType) {
        this.sfemType = sfemType;
        this.name = name;
        this.productionHistory = new TreeMap<>();
    }

    public SFEM_type getSfemType() {
        return sfemType;
    }

    public String getName() {
        return name;
    }

    public void addPartToProductionHistory(part_prodTime producedPart) {
        productionHistory.put(producedPart.part().getId(), producedPart);
    }

    public TreeMap<Integer, part_prodTime> getProductionHistory() {
        return productionHistory;
    }
}
