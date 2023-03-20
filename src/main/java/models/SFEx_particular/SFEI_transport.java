package models.SFEx_particular;

import models.base.SFEI;
import models.sensor_actuator;

import java.time.Instant;

public class SFEI_transport extends SFEI {

    private sensor_actuator aRemover;

    private sensor_actuator aEmitter;
    private sensor_actuator aEmitterPart;
    private sensor_actuator aEmitterBase;

    public SFEI_transport(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                          Instant dayOfBirth, Instant dayOfLastMaintenance, sensor_actuator aRemover,
                          sensor_actuator aEmitter, sensor_actuator aEmitterpart, sensor_actuator aEmitterBase) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance);
        this.aRemover = aRemover;
        this.aEmitter = aEmitter;
        this.aEmitterPart = aEmitterpart;
        this.aEmitterBase = aEmitterBase;
    }

    public sensor_actuator getaRemover() {
        return aRemover;
    }

    public sensor_actuator getaEmitter() {
        return aEmitter;
    }

    public sensor_actuator getaEmitterPart() {
        return aEmitterPart;
    }

    public sensor_actuator getaEmitterBase() {
        return aEmitterBase;
    }
}
