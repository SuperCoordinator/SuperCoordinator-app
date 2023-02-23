package models.SFEI;

import models.part;
import models.sensor_actuator;

import java.time.Instant;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

public class SFEI {

    private final String name;
    private final sensor_actuator inSensor;
    private final sensor_actuator outSensor;
    private final TreeSet<part> partsATM;
    private int nPiecesMoved;
    private final Instant dayOfBirth;
    private Instant dayOfLastMaintenance;
    private long minOperationTime;

    public SFEI(String name, sensor_actuator inSensor, sensor_actuator outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance) {
        this.name = name;
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

    public sensor_actuator getInSensor() {
        return inSensor;
    }

    public sensor_actuator getOutSensor() {
        return outSensor;
    }

    public TreeSet<part> getPartsATM() {
        return partsATM;
    }

    public void addNewPartATM(part partATM) {
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
