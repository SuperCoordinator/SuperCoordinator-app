package models.SFEx_particular;

import models.base.SFEI;
import models.base.part;
import models.partsAspect;
import models.sensor_actuator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;

public class SFEI_transport extends SFEI implements Externalizable {

    public static final long serialVersionUID = 1234L;
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(aRemover);
        out.writeObject(aEmitter);
        out.writeObject(aEmitterPart);
        out.writeObject(aEmitterBase);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.aRemover = (sensor_actuator) in.readObject();
        this.aEmitter = (sensor_actuator) in.readObject();
        this.aEmitterPart = (sensor_actuator) in.readObject();
        this.aEmitterBase = (sensor_actuator) in.readObject();

    }

    private sensor_actuator aRemover;

    private sensor_actuator aEmitter;
    private sensor_actuator aEmitterPart;
    private sensor_actuator aEmitterBase;

    public SFEI_transport() {
    }

    public SFEI_transport(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                          Instant dayOfBirth, Instant dayOfLastMaintenance, sensor_actuator aRemover,
                          sensor_actuator aEmitter, sensor_actuator aEmitterpart, sensor_actuator aEmitterBase) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance, false, false);
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
