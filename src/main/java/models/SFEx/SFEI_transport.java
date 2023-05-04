package models.SFEx;

import models.base.SFEI;
import models.sensor_actuator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

//@XmlRootElement(name = "SFEI_transport")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEI_transport extends SFEI  {

    @XmlElement
    private sensor_actuator aRemover;
    @XmlElement
    private sensor_actuator aEmitter;
    @XmlElement
    private sensor_actuator aEmitterPart;
    @XmlElement
    private sensor_actuator aEmitterBase;

    public SFEI_transport() {
    }

    public SFEI_transport(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                          Instant dayOfBirth, Instant dayOfLastMaintenance, sensor_actuator aRemover,
                          sensor_actuator aEmitter, sensor_actuator aEmitterpart, sensor_actuator aEmitterBase) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance,true, false, false, false);
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
