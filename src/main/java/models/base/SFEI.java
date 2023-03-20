package models.base;

import failures.newVersion.failure_occurrence;
import models.sensor_actuator;
import org.apache.commons.math3.util.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

public class SFEI {

    public enum SFEI_type {
        CONVEYOR,
        MACHINE,
        TRANSPORT
    }

    private final String name;
    private final SFEI_type sfeiType;
    private final sensor_actuator inSensor;
    private final sensor_actuator outSensor;
    private long minOperationTime;
    private final TreeSet<part> partsATM;
    private int nPiecesMoved;
    private final Instant dayOfBirth;
    private Instant dayOfLastMaintenance;

    private TreeMap<Integer, failure_occurrence> failuresHistory;

    private final ArrayList<Pair<Integer, Instant>> breakdownHistory;

    private boolean forTransport = false;

    public SFEI(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance) {
        this.name = name;
        this.sfeiType = sfeiType;
        this.inSensor = inSensor;
        this.outSensor = outSensor;
        this.dayOfBirth = dayOfBirth;
        this.dayOfLastMaintenance = dayOfLastMaintenance;

        this.nPiecesMoved = 0;
        this.partsATM = new TreeSet<>(Comparator.comparing(part::getId));

        this.failuresHistory = new TreeMap<>();

        this.breakdownHistory = new ArrayList<>();
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

    public long getMinOperationTime() {
        return minOperationTime;
    }

    public void setMinOperationTime(long minOperationTime) {
        this.minOperationTime = minOperationTime;
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

    public TreeMap<Integer, failure_occurrence> getFailuresHistory() {
        return failuresHistory;
    }

    public void addNewFailureOccurrence(failure_occurrence failureOcurrence) {
        failuresHistory.put(failuresHistory.size(), failureOcurrence);
    }

    public Pair<Integer, Instant> getLastBreakdown() {
        return breakdownHistory.get(breakdownHistory.size() - 1);
    }

    public void addBreakdown(Pair<Integer, Instant> event) {
        breakdownHistory.add(event);
    }

    public boolean isForTransport() {
        return forTransport;
    }

    public void setForTransport(boolean forTransport) {
        this.forTransport = forTransport;
    }
}
