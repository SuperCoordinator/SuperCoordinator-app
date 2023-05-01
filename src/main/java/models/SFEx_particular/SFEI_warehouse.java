package models.SFEx_particular;

import models.base.SFEI;
import models.sensor_actuator;

import java.time.Instant;

public class SFEI_warehouse extends SFEI {

    public SFEI_warehouse() {
        super("sfei_warehouse", SFEI_type.WAREHOUSE, null, null, Instant.now(), Instant.now(), true, false, false, false);
    }
}
