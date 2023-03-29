package models.SFEx_particular;

import models.base.SFEI;
import models.sensor_actuator;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;

//@XmlRootElement(name = "SFEI_conveyor")
@XmlRootElement(name = "SFEI")
public class SFEI_conveyor extends SFEI implements Externalizable {
    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(aRemover);
        out.writeObject(aEmitter);
        out.writeObject(sRemover);
        out.writeObject(sEmitter);
        out.writeObject(aConveyorMotor);
        out.writeBoolean(isSimulation);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.aRemover = (sensor_actuator) in.readObject();
        this.aEmitter = (sensor_actuator) in.readObject();
        this.sRemover = (sensor_actuator) in.readObject();
        this.sEmitter = (sensor_actuator) in.readObject();
        this.aConveyorMotor = (sensor_actuator) in.readObject();
        this.isSimulation = in.readBoolean();

    }

    private sensor_actuator aRemover;
    private sensor_actuator aEmitter;
    private sensor_actuator sRemover;
    private sensor_actuator sEmitter;
    private sensor_actuator aConveyorMotor;

    private boolean isSimulation;

    public SFEI_conveyor() {
        super();
    }

    public SFEI_conveyor(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                         Instant dayOfBirth, Instant dayOfLastMaintenance, boolean line_start, boolean line_end,
                         sensor_actuator aConveyorMotor, sensor_actuator[] sensorActuators) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, line_start, line_end);
        this.aRemover = sensorActuators[0];
        this.aEmitter = sensorActuators[1];
        this.sRemover = sensorActuators[2];
        this.sEmitter = sensorActuators[3];
        this.aConveyorMotor = aConveyorMotor;
        this.isSimulation = true;
    }

    public SFEI_conveyor(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                         Instant dayOfBirth, Instant dayOfLastMaintenance, boolean line_start, boolean line_end,
                         sensor_actuator aConveyorMotor) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, line_start, line_end);
        this.aConveyorMotor = aConveyorMotor;
        this.isSimulation = false;
    }

    @XmlElement(name = "remover_actuator")
    public sensor_actuator getaRemover() {
        return aRemover;
    }

    @XmlElement(name = "emitter_actuator")
    public sensor_actuator getaEmitter() {
        return aEmitter;
    }

    @XmlElement(name = "remover_sensor")
    public sensor_actuator getsRemover() {
        return sRemover;
    }

    @XmlElement(name = "emitter_sensor")
    public sensor_actuator getsEmitter() {
        return sEmitter;
    }

    @XmlElement(name = "conveyor_motor")
    public sensor_actuator getaConveyorMotor() {
        return aConveyorMotor;
    }

    @XmlAttribute(name = "is_simulation")
    public boolean isSimulation() {
        return isSimulation;
    }
}
