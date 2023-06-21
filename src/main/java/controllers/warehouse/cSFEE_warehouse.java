package controllers.warehouse;

import models.sfe_x.SFEI_warehouse;
import models.sfe_x.SFEM_warehouse;
import models.base.SFEE;
import monitors.warehouse.SFEE_warehouse_monitor;

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

    public void init(int checkOrders_period, SFEM_warehouse.warehouseOrganization warehouseOrganization) {
        try {
            // Entry SFEI
            sfee.getSFEIs().put(0, new SFEI_warehouse("sfei_entryWarehouse"));
            // Exit SFEI
            sfee.getSFEIs().put(1, new SFEI_warehouse("sfei_exitWarehouse"));

            // Load the warehouse with parts that was previously on it to be used, this also update index of partID in the warehouse
            sfeeWarehouseMonitor = new SFEE_warehouse_monitor(sfee, checkOrders_period,warehouseOrganization);
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
