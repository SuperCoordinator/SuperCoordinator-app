package controllers.warehouse;

import models.SFEx_particular.SFEM_warehouse;
import models.base.SFEE;
import monitor.SFEM_warehouse_monitor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEM_warehouse implements Runnable {


    @XmlElement
    private SFEM_warehouse sfem;

    private SFEM_warehouse_monitor sfemWarehouseMonitor;

    @XmlElement
    private cSFEE_warehouse sfeeWarehouseController;

    public cSFEM_warehouse() {
    }

    public cSFEM_warehouse(SFEM_warehouse sfem) {
        this.sfem = sfem;
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
        sfemWarehouseMonitor = new SFEM_warehouse_monitor(0);
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
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
