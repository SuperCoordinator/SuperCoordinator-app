package controllers.warehouse;

import models.SFEx.SFEI_warehouse;
import models.base.SFEE;
import models.base.part;
import monitor.warehouse.SFEE_warehouse_monitor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEE_warehouse {


    private SFEE sfee;

    private SFEE_warehouse_monitor sfeeWarehouseMonitor;

    public cSFEE_warehouse() {
    }

    public cSFEE_warehouse(SFEE sfee) {
        this.sfee = sfee;
    }

    public SFEE getSfee() {
        return sfee;
    }

    public void init(int checkOrders_period) {
        // Create SFEI
        SFEI_warehouse sfeiWarehouse = new SFEI_warehouse();
        sfee.getSFEIs().put(0, sfeiWarehouse);

        // This part_id_offset should be a query in DB
        sfeeWarehouseMonitor = new SFEE_warehouse_monitor(0, checkOrders_period);
    }

    public void loadWHBasedOnPrevStock() {
        sfeeWarehouseMonitor.loadWHBasedOnPrevStock();
        storeParts(sfeeWarehouseMonitor.getRecentArrivedParts());
    }

    public void storeParts(ArrayList<part> recentArrivedParts) {
        sfee.getSFEIs().get(0).getPartsATM().addAll(recentArrivedParts);
        System.out.println("#parts in the warehouse: " + sfee.getSFEIs().get(0).getPartsATM().size());
    }

    public void loop() {
        if (sfeeWarehouseMonitor.loop()) {
            storeParts(sfeeWarehouseMonitor.getRecentArrivedParts());
            sfeeWarehouseMonitor.clearStoredParts();
        }
    }


}
