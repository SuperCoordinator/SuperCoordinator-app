package models;

//import monitor.stochasticTime.conveyor;

import java.util.ArrayList;
import java.util.TreeMap;

public class SFEM {

    private String name;

    private final TreeMap<Integer, SFEE> SFEEs;

    private final ArrayList<producedPart> productionHistory;

    public SFEM(String name) {
        this.name = name;
        this.SFEEs = new TreeMap<>();
        this.productionHistory = new ArrayList<>();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeMap<Integer, SFEE> getSFEEs() {
        return SFEEs;
    }

    public SFEE getSFEEbyIndex(int index) {
        return SFEEs.get(index);
    }

    public void addNewSFEE(SFEE newSFEE) {
        int index = SFEEs.size();
        SFEEs.put(index, newSFEE);
    }

    public void addPartToProductionHistory(producedPart producedPart) {
        productionHistory.add(productionHistory.size(), producedPart);
    }

    public ArrayList<producedPart> getProductionHistory() {
        return productionHistory;
    }
}
