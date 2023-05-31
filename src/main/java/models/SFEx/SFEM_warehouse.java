package models.SFEx;

import models.base.SFEE;
import models.base.SFEM;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM_warehouse extends SFEM {

    public enum warehouseOrganization {
        RANDOM,
        SEQUENTIAL
    }

    @XmlElement
    private SFEE sfeeWarehouse;
    @XmlAttribute
    private warehouseOrganization warehouseOrganization;
    public SFEM_warehouse() {
        super();
    }
    public SFEM_warehouse(warehouseOrganization warehouseOrganization) {
        super("sfem_warehouse", SFEM_role.WAREHOUSE);
        this.warehouseOrganization = warehouseOrganization;
    }

    public void setSfeeWarehouse(SFEE sfeeWarehouse) {
        this.sfeeWarehouse = sfeeWarehouse;
    }

    public SFEE getSfeeWarehouse() {
        return sfeeWarehouse;
    }

    public void setWarehouseOrganization(SFEM_warehouse.warehouseOrganization warehouseOrganization) {
        this.warehouseOrganization = warehouseOrganization;
    }

    public warehouseOrganization getWHDistribution() {
        return warehouseOrganization;
    }
}
