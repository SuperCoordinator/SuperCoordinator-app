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

//@XmlRootElement(name = "SFEI_machine")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEI_machine extends SFEI implements Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(partForm);
        out.writeObject(aProduce);
        out.writeObject(sDoor);
        out.writeObject(aStop);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.partForm = (partsAspect.form) in.readObject();
        this.aProduce = (sensor_actuator) in.readObject();
        this.sDoor = (sensor_actuator) in.readObject();
        this.aStop = (sensor_actuator) in.readObject();

    }

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

    //    @XmlAttribute(name = "part_form")
    public partsAspect.form getPartForm() {
        return partForm;
    }

    //    @XmlElement(name = "produce_actuator")
    public sensor_actuator getaProduce() {
        return aProduce;
    }

    //    @XmlElement(name = "door_sensor")
    public sensor_actuator getsDoor() {
        return sDoor;
    }

    //    @XmlElement(name = "stop_actuator")
    public sensor_actuator getaStop() {
        return aStop;
    }


}
