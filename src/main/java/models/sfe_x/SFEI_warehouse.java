package models.sfe_x;

import models.base.SFEI;
import models.base.part;

import java.time.Instant;
import java.util.TreeSet;

public class SFEI_warehouse extends SFEI {
    public SFEI_warehouse(String name) {
        super(name, SFEI_type.WAREHOUSE, null, null, Instant.now(), Instant.now(), 0, true, false, false, false);
    }

    @Override
    public void setPartsATM(TreeSet<part> partsATM) {
        super.setPartsATM(partsATM);
    }
}
