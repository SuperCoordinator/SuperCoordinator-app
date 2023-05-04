package controllers.warehouse;

import models.SFEx.SFEM_warehouse;
import models.base.SFEE;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEM_warehouse implements Runnable {
    @XmlAttribute
    private int checkOrders_period;
    @XmlElement
    private SFEM_warehouse sfem;
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
                SFEE.SFEE_environment.SIMULATION,
                SFEE.SFEE_role.WAREHOUSE,
                SFEE.communicationOption.MODBUS);
        sfem.setSfeeWarehouse(sfeeWarehouse);

        sfeeWarehouseController = new cSFEE_warehouse(sfeeWarehouse);
        sfeeWarehouseController.init(checkOrders_period);


    }

    public void init_afterLoad() {
        sfeeWarehouseController = new cSFEE_warehouse(sfem.getSfeeWarehouse());
        sfeeWarehouseController.init(checkOrders_period);

    }


    public cSFEE_warehouse getSfeeWarehouseController() {
        return sfeeWarehouseController;
    }

    @Override
    public void run() {
        try {
            sfeeWarehouseController.loop();



        } catch (Exception e) {
            // In child thread, it must print the Exception because the main thread do not catch Runtime Exception from the others
            e.printStackTrace();
        }
    }
}
