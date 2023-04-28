package models.SFEx_particular;

import models.base.SFEE;
import models.base.SFEM;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM_warehouse extends SFEM {

    @XmlElement
    private SFEE sfeeWarehouse;

    public SFEM_warehouse() {
        super("sfem_warehouse", SFEM_type.WAREHOUSE);
    }

    public void setSfeeWarehouse(SFEE sfeeWarehouse) {
        this.sfeeWarehouse = sfeeWarehouse;
    }

    public SFEE getSfeeWarehouse() {
        return sfeeWarehouse;
    }
}
