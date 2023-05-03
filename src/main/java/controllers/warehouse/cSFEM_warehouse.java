package controllers.warehouse;

import models.SFEx_particular.SFEM_warehouse;
import models.base.SFEE;
import monitor.warehouse.SFEM_warehouse_monitor;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEM_warehouse implements Runnable {
    @XmlAttribute
    private int checkOrders_period;
    @XmlElement
    private SFEM_warehouse sfem;

    private SFEM_warehouse_monitor sfemWarehouseMonitor;

    private cSFEE_warehouse sfeeWarehouseController;

    public cSFEM_warehouse() {
    }

    public cSFEM_warehouse(SFEM_warehouse sfem, int checkOrders_period) {
        this.sfem = sfem;
        this.checkOrders_period = checkOrders_period;
    }

    public SFEM_warehouse getSfem() {
        return sfem;
    }

    public void init(String SFEE_warehouse_name) {
        SFEE sfeeWarehouse = new SFEE(SFEE_warehouse_name,
                SFEE.SFEE_type.SIMULATION,
                SFEE.SFEE_function.WAREHOUSE,
                SFEE.communicationOption.MODBUS);
        sfem.setSfeeWarehouse(sfeeWarehouse);

        sfeeWarehouseController = new cSFEE_warehouse(sfeeWarehouse);
        sfeeWarehouseController.init();

        // This part_id_offset should be a query in DB
        sfemWarehouseMonitor = new SFEM_warehouse_monitor(0,checkOrders_period);
    }

    public void init_afterLoad() {
        sfeeWarehouseController = new cSFEE_warehouse(sfem.getSfeeWarehouse());
        sfeeWarehouseController.init();

        // This part_id_offset should be a query in DB
        sfemWarehouseMonitor = new SFEM_warehouse_monitor(0,checkOrders_period);
    }


    public void loadWHBasedOnPrevStock(){
        sfemWarehouseMonitor.loadWHBasedOnPrevStock();
        sfeeWarehouseController.storeParts(sfemWarehouseMonitor.getRecentArrivedParts());
    }

    public cSFEE_warehouse getSfeeWarehouseController() {
        return sfeeWarehouseController;
    }

    @Override
    public void run() {
        try {
            sfeeWarehouseController.loop();

            if (sfemWarehouseMonitor.loop()) {
                sfeeWarehouseController.storeParts(sfemWarehouseMonitor.getRecentArrivedParts());
                sfemWarehouseMonitor.clearStoredParts();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
