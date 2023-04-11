package models.SFEx_particular;

import models.base.SFEI;
import models.partsAspect;
import models.sensor_actuator;

import javax.xml.bind.annotation.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEI_machine extends SFEI {

    @XmlAttribute
    private partsAspect.form partForm;
    @XmlElement
    private sensor_actuator aProduce;
    @XmlElement
    private sensor_actuator sDoor;
    @XmlElement
    private sensor_actuator aStop;

    public SFEI_machine() {
    }

    public SFEI_machine(String name, SFEI_type sfeiType, partsAspect.form partForm, sensor_actuator inSensor, sensor_actuator outSensor,
                        Instant dayOfBirth, Instant dayOfLastMaintenance, boolean isSimulation, boolean supportsFailures,
                        boolean line_start, boolean line_end, sensor_actuator[] sensorActuators) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, isSimulation, supportsFailures, line_start, line_end);
        this.partForm = partForm;
        if (isSimulation) {
            this.aProduce = sensorActuators[0];
            if (supportsFailures) {
                this.sDoor = sensorActuators[1];
                this.aStop = sensorActuators[2];
            }
        }else {
            this.aStop = sensorActuators[2];
        }
    }

    public partsAspect.form getPartForm() {
        return partForm;
    }

    public sensor_actuator getaProduce() {
        return aProduce;
    }

    public sensor_actuator getsDoor() {
        return sDoor;
    }

    public sensor_actuator getaStop() {
        return aStop;
    }


}
