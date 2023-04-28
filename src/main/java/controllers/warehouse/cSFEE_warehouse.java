package controllers.warehouse;

import models.SFEx_particular.SFEI_warehouse;
import models.base.SFEE;
import models.base.part;

import java.util.ArrayList;

public class cSFEE_warehouse {

    private SFEE sfee;

    public cSFEE_warehouse() {
    }

    public cSFEE_warehouse(SFEE sfee) {
        this.sfee = sfee;
    }

    public void init() {
        // Create SFEI
        SFEI_warehouse sfeiWarehouse = new SFEI_warehouse();
        sfee.getSFEIs().put(0, sfeiWarehouse);

    }

    public void storeParts(ArrayList<part> recentArrivedParts) {
        sfee.getSFEIs().get(0).getPartsATM().addAll(recentArrivedParts);
    }

    public void loop() {
    }
}
