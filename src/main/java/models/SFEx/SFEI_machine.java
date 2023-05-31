package models.SFEx;

import models.base.SFEI;
import models.partDescription;
import models.sensor_actuator;

import javax.xml.bind.annotation.*;
import java.time.Instant;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEI_machine extends SFEI {

    @XmlElement
    private partDescription partDescription;
    @XmlElement
    private sensor_actuator aProduce;
    @XmlElement
    private sensor_actuator sDoor;
    @XmlElement
    private sensor_actuator aStop;

    public SFEI_machine() {
    }

    public SFEI_machine(String name, partDescription partForm, sensor_actuator inSensor, sensor_actuator outSensor,
                        Instant dayOfBirth, Instant dayOfLastMaintenance, boolean isSimulation, boolean supportsFailures,
                        sensor_actuator[] sensorActuators) {
        super(name, SFEI_type.MACHINE, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, 34, isSimulation, supportsFailures, false, false);

        this.partDescription = partForm;

        if (isSimulation) {
            this.aProduce = sensorActuators[0];
            if (supportsFailures) {
                this.sDoor = sensorActuators[1];
                this.aStop = sensorActuators[2];
            }
        } else {
            this.aStop = sensorActuators[2];
        }
    }

    public partDescription getPartDescription() {
        return partDescription;
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
