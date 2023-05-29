package models.SFEx;

import models.base.SFEI;
import models.sensor_actuator;

import javax.xml.bind.annotation.*;
import java.time.Instant;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)

public class SFEI_conveyor extends SFEI {
    @XmlElement
    private sensor_actuator aRemover;
    @XmlElement
    private sensor_actuator aEmit;
    @XmlElement
    private sensor_actuator aEmitPart;
    @XmlElement
    private sensor_actuator aEmitBase;
    @XmlElement
    private sensor_actuator sRemover;
    @XmlElement
    private sensor_actuator sEmitter;
    @XmlElement
    private sensor_actuator aConveyorMotor;


    public SFEI_conveyor() {
        super();
    }

    public SFEI_conveyor(String name, sensor_actuator inSensor, sensor_actuator outSensor,
                         Instant dayOfBirth, Instant dayOfLastMaintenance, boolean isSimulation,
                         boolean supportsFailures, sensor_actuator[] sensorActuators) {
        super(name, SFEI_type.CONVEYOR, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, 8, isSimulation, supportsFailures, false, false);

        if (supportsFailures && isSimulation) {
            this.aRemover = sensorActuators[0];
            this.aEmit = sensorActuators[1];
            this.aEmitPart = sensorActuators[2];
            this.aEmitBase = sensorActuators[3];
            this.sRemover = sensorActuators[4];
            this.sEmitter = sensorActuators[5];

        }
        this.aConveyorMotor = sensorActuators[6];

    }

    public sensor_actuator getaRemover() {
        return aRemover;
    }

    public sensor_actuator getaEmit() {
        return aEmit;
    }

    public sensor_actuator getaEmitPart() {
        return aEmitPart;
    }

    public sensor_actuator getaEmitBase() {
        return aEmitBase;
    }

    public sensor_actuator getsRemover() {
        return sRemover;
    }


    public sensor_actuator getsEmitter() {
        return sEmitter;
    }


    public sensor_actuator getaConveyorMotor() {
        return aConveyorMotor;
    }


}
