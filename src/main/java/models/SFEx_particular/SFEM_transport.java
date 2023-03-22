package models.SFEx_particular;

import models.base.SFEE;
import models.base.SFEI;
import models.base.SFEM;
import models.part_prodTime;

import java.util.ArrayList;
import java.util.TreeMap;

public class SFEM_transport extends SFEM {

    private SFEE sfeeTransport;

    private final TreeMap<Integer, part_prodTime> productionHistory;

    public SFEM_transport(String name) {
        super(name, SFEM_type.TRANSPORT);
        this.productionHistory = new TreeMap<>();
    }

    public void setSfeeTransport(SFEE sfeeTransport) {
        this.sfeeTransport = sfeeTransport;
    }

    public SFEE getSfeeTransport() {
        return sfeeTransport;
    }

    public void addPartToProductionHistory(part_prodTime producedPart) {
        productionHistory.put(productionHistory.size(), producedPart);
    }

    public TreeMap<Integer, part_prodTime> getProductionHistory() {
        return productionHistory;
    }

}
