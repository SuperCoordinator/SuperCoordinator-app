package controllers.warehouse;

import models.SFEx.SFEI_warehouse;
import models.base.SFEE;
import models.base.SFEI;
import models.base.part;
import monitor.warehouse.SFEE_warehouse_monitor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

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
        try {
            // Entry SFEI
            sfee.getSFEIs().put(0, new SFEI_warehouse("sfei_entryWarehouse"));
            // Exit SFEI
            sfee.getSFEIs().put(1, new SFEI_warehouse("sfei_exitWarehouse"));

            // Load the warehouse with parts that was previously on it to be used, this also update index of partID in the warehouse
            sfeeWarehouseMonitor = new SFEE_warehouse_monitor(sfee, checkOrders_period);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public void loadWHBasedOnPrevStock() {
        try {
            sfeeWarehouseMonitor.loadWHBasedOnPrevStock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loop() {
        try {
            sfeeWarehouseMonitor.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
