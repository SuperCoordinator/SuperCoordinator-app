package models.SFEI;

import models.sensor_actuator;

import java.time.Instant;

public class SFEI_machine extends SFEI {

    private final sensor_actuator sDoor;
    private final sensor_actuator aStop;

    public SFEI_machine(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance,
                        sensor_actuator sDoor, sensor_actuator aStop) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance);
        this.sDoor = sDoor;
        this.aStop = aStop;
    }

    public sensor_actuator getsDoor() {
        return sDoor;
    }

    public sensor_actuator getaStop() {
        return aStop;
    }
}
