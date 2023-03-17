package models.SFEx_particular;

import communication.modbus;
import models.base.SFEE;
import models.sensor_actuator;
import org.apache.commons.math3.util.Pair;

public class SFEM_transport extends SFEM_production {

    // SFEM_transport based on 1-1 connections between SFEE's
    private Pair<sensor_actuator, sensor_actuator> inSensAct;
    private Pair<sensor_actuator, sensor_actuator> outSensAct;

    public SFEM_transport(String name) {
        super(name);
    }

    public Pair<sensor_actuator, sensor_actuator> getInSensAct() {
        return inSensAct;
    }

    public void setInSensAct(Pair<sensor_actuator, sensor_actuator> inSensAct) {
        this.inSensAct = inSensAct;
    }

    public Pair<sensor_actuator, sensor_actuator> getOutSensAct() {
        return outSensAct;
    }

    public void setOutSensAct(Pair<sensor_actuator, sensor_actuator> outSensAct) {
        this.outSensAct = outSensAct;
    }
}
