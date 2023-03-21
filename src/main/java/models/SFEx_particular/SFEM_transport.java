package models.SFEx_particular;

import communication.modbus;
import models.base.SFEE;
import models.base.SFEM;
import models.part_prodTime;
import models.sensor_actuator;
import org.apache.commons.math3.util.Pair;
import viewers.SFEE_transport;

import java.util.ArrayList;

public class SFEM_transport extends SFEM {

    private SFEE sfeeTransport;

    private final ArrayList<part_prodTime> productionHistory;

    public SFEM_transport(String name) {
        super(name, SFEM_type.TRANSPORT);
        this.productionHistory = new ArrayList<>();
    }

    public void setSfeeTransport(SFEE sfeeTransport) {
        this.sfeeTransport = sfeeTransport;
    }

    public SFEE getSfeeTransport() {
        return sfeeTransport;
    }

    public void addPartToProductionHistory(part_prodTime producedPart) {
        productionHistory.add(productionHistory.size(), producedPart);
    }

    public ArrayList<part_prodTime> getProductionHistory() {
        return productionHistory;
    }
}
