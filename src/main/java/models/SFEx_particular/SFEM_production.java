package models.SFEx_particular;

//import monitor.stochasticTime.conveyor;

import models.base.SFEE;
import models.base.SFEM;
import models.part_prodTime;

import java.util.ArrayList;
import java.util.TreeMap;

public class SFEM_production extends SFEM {

    private final TreeMap<Integer, SFEE> SFEEs;
    private final TreeMap<Integer, part_prodTime> productionHistory;

    public SFEM_production(String name) {
        super(name, SFEM_type.PRODUCTION);
        this.SFEEs = new TreeMap<>();
        this.productionHistory = new TreeMap<>();

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

    public void addPartToProductionHistory(part_prodTime producedPart) {
        productionHistory.put(producedPart.part().getId(), producedPart);
    }

    public TreeMap<Integer, part_prodTime> getProductionHistory() {
        return productionHistory;
    }
}
