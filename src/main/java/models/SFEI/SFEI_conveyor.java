package models.SFEI;

import models.sensor_actuator;

import java.time.Instant;

public class SFEI_conveyor extends SFEI {

    private sensor_actuator aRemover;
    private sensor_actuator aEmitter;
    private sensor_actuator sRemover;
    private sensor_actuator sEmitter;
    private sensor_actuator aConveyorMotor;

    private final boolean isSimulation;

    public SFEI_conveyor(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                         Instant dayOfBirth, Instant dayOfLastMaintenance, sensor_actuator[] sensorActuators) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance);
        this.aRemover = sensorActuators[0];
        this.aEmitter = sensorActuators[1];
        this.sRemover = sensorActuators[2];
        this.sEmitter = sensorActuators[3];

        this.isSimulation = true;
    }

    public SFEI_conveyor(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                         Instant dayOfBirth, Instant dayOfLastMaintenance, sensor_actuator aConveyorMotor) {
        super(name, sfeiType, inSensor, outSensor, dayOfBirth, dayOfLastMaintenance);
        this.aConveyorMotor = aConveyorMotor;
        this.isSimulation = false;
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

    public boolean isSimulation() {
        return isSimulation;
    }
}
