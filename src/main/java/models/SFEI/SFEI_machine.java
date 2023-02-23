package models.SFEI;

import models.sensor_actuator;

import java.time.Instant;

public class SFEI_machine extends SFEI {

    private final sensor_actuator aStop;

    public SFEI_machine(String name, sensor_actuator inSensor, sensor_actuator outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, sensor_actuator aStop) {
        super(name, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance);
        this.aStop = aStop;
    }

    public sensor_actuator getaStop() {
        return aStop;
    }
}
