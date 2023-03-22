package models.SFEx_particular;

import models.base.SFEI;
import models.partsAspect;
import models.sensor_actuator;

import java.time.Instant;

public class SFEI_machine extends SFEI {

    private final partsAspect.form partForm;
    private final sensor_actuator aProduce;
    private final sensor_actuator sDoor;
    private final sensor_actuator aStop;

    public SFEI_machine(String name, SFEI_type sfeiType, partsAspect.form partForm, sensor_actuator inSensor, sensor_actuator outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance,
                        boolean line_start, boolean line_end, sensor_actuator aProduce, sensor_actuator sDoor, sensor_actuator aStop) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance,line_start,line_end);
        this.partForm = partForm;
        this.aProduce = aProduce;
        this.sDoor = sDoor;
        this.aStop = aStop;
    }

    public partsAspect.form getPartForm() {
        return partForm;
    }

    public sensor_actuator getsDoor() {
        return sDoor;
    }

    public sensor_actuator getaStop() {
        return aStop;
    }

    public sensor_actuator getaProduce() {
        return aProduce;
    }
}
