package models.SFEx_particular;

import models.base.SFEI;
import models.sensor_actuator;

import javax.xml.bind.annotation.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)

public class SFEI_conveyor extends SFEI  {
    @XmlElement
    private sensor_actuator aRemover;
    @XmlElement
    private sensor_actuator aEmitter;
    @XmlElement
    private sensor_actuator sRemover;
    @XmlElement
    private sensor_actuator sEmitter;
    @XmlElement
    private sensor_actuator aConveyorMotor;


    public SFEI_conveyor() {
        super();
    }

    public SFEI_conveyor(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                         Instant dayOfBirth, Instant dayOfLastMaintenance, boolean isSimulation,
                         boolean supportsFailures, boolean line_start, boolean line_end, sensor_actuator[] sensorActuators) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, isSimulation, supportsFailures, line_start, line_end);

        if (supportsFailures && isSimulation) {
            this.aRemover = sensorActuators[0];
            this.aEmitter = sensorActuators[1];
            this.sRemover = sensorActuators[2];
            this.sEmitter = sensorActuators[3];

        }
        this.aConveyorMotor = sensorActuators[4];

    }

    public sensor_actuator getaRemover() {
        return aRemover;
    }

    public sensor_actuator getaEmitter() {
        return aEmitter;
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
