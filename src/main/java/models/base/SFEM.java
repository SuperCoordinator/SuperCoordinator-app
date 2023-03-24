package models.base;

import models.part_prodTime;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.TreeMap;

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

        this.productionHistory = new TreeMap<>();
    }

    public enum SFEM_type {
        PRODUCTION,
        TRANSPORT
    }

    private SFEM_type sfemType;
    private String name;
    private TreeMap<Integer, part_prodTime> productionHistory;

    public SFEM() {
    }

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
