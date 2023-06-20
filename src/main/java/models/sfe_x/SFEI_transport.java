package models.sfe_x;

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
public class SFEI_transport extends SFEI {

    @XmlElement
    private sensor_actuator aRemover;
    @XmlElement
    private sensor_actuator aEmit;
    @XmlElement
    private sensor_actuator aEmitPart;
    @XmlElement
    private sensor_actuator aEmitBase;

    public SFEI_transport() {
    }

    public SFEI_transport(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                          Instant dayOfBirth, Instant dayOfLastMaintenance, sensor_actuator aRemover,
                          sensor_actuator aEmit, sensor_actuator aEmitterpart, sensor_actuator aEmitBase) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, 0, true, false, false, false);
        this.aRemover = aRemover;
        this.aEmit = aEmit;
        this.aEmitPart = aEmitterpart;
        this.aEmitBase = aEmitBase;
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

}
