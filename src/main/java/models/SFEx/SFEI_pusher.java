package models.SFEx;

import models.base.SFEI;
import models.sensor_actuator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)

public class SFEI_pusher extends SFEI {

    @XmlElement
    private sensor_actuator aForwardMotor;
    @XmlElement
    private sensor_actuator aBackMotor;

    public SFEI_pusher() {
        super();
    }

    public SFEI_pusher(String name, sensor_actuator inSensor, sensor_actuator outSensor,
                       Instant dayOfBirth, Instant dayOfLastMaintenance, boolean isSimulation,
                       boolean supportsFailures, boolean line_start, boolean line_end, sensor_actuator aBackMotor, sensor_actuator aForwardMotor) {
        super(name, SFEI_type.PUSHER, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, isSimulation, supportsFailures, line_start, line_end);

        this.aBackMotor = aBackMotor;
        this.aForwardMotor = aForwardMotor;

    }

    public sensor_actuator getsPusherForward() {
        return super.getOutSensor();
    }

    public sensor_actuator getsPusherBack() {
        return super.getInSensor();
    }

    public sensor_actuator getaForwardMotor() {
        return aForwardMotor;
    }

    public sensor_actuator getaBackMotor() {
        return aBackMotor;
    }
}
