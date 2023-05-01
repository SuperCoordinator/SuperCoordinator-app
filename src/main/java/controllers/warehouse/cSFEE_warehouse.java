package controllers.warehouse;

import models.SFEx_particular.SFEI_warehouse;
import models.base.SFEE;
import models.base.part;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEE_warehouse {


    private SFEE sfee;

    public cSFEE_warehouse() {
    }

    public cSFEE_warehouse(SFEE sfee) {
        this.sfee = sfee;
    }

    public SFEE getSfee() {
        return sfee;
    }

    public void init() {
        // Create SFEI
        SFEI_warehouse sfeiWarehouse = new SFEI_warehouse();
        sfee.getSFEIs().put(0, sfeiWarehouse);

    }


    public void storeParts(ArrayList<part> recentArrivedParts) {
        sfee.getSFEIs().get(0).getPartsATM().addAll(recentArrivedParts);
        System.out.println("#parts in the warehouse: " + sfee.getSFEIs().get(0).getPartsATM().size());
    }


    public void loop() {
    }


}
