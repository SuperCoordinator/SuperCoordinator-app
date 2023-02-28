package models.SFEI;

import models.part;
import models.sensor_actuator;

import java.time.Instant;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

public class SFEI {

    public enum SFEI_type {
        CONVEYOR,
        MACHINE
    }

    private final String name;

    private final SFEI_type sfeiType;
    private final sensor_actuator inSensor;
    private final sensor_actuator outSensor;
    private final TreeSet<part> partsATM;
    private int nPiecesMoved;
    private final Instant dayOfBirth;
    private Instant dayOfLastMaintenance;
    private long minOperationTime;

    public SFEI(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance) {
        this.name = name;
        this.sfeiType = sfeiType;
        this.inSensor = inSensor;
        this.outSensor = outSensor;
        this.dayOfBirth = dayOfBirth;
        this.dayOfLastMaintenance = dayOfLastMaintenance;

        this.nPiecesMoved = 0;
        this.partsATM = new TreeSet<>(Comparator.comparing(part::getId));
    }

    public String getName() {
        return name;
    }

    public SFEI_type getSfeiType() {
        return sfeiType;
    }

    public sensor_actuator getInSensor() {
        return inSensor;
    }

    public sensor_actuator getOutSensor() {
        return outSensor;
    }

    public synchronized TreeSet<part> getPartsATM() {
        return partsATM;
    }

    public synchronized void addNewPartATM(part partATM) {
        this.partsATM.add(partATM);
    }

    public int getnPiecesMoved() {
        return nPiecesMoved;
    }

    public void setnPiecesMoved(int nPiecesMoved) {
        this.nPiecesMoved = nPiecesMoved;
    }

    public Instant getDayOfBirth() {
        return dayOfBirth;
    }

    public Instant getDayOfLastMaintenance() {
        return dayOfLastMaintenance;
    }

    public void setDayOfLastMaintenance(Instant dayOfLastMaintenance) {
        this.dayOfLastMaintenance = dayOfLastMaintenance;
    }

    public long getMinOperationTime() {
        return minOperationTime;
    }

    public void setMinOperationTime(long minOperationTime) {
        this.minOperationTime = minOperationTime;
    }
}
