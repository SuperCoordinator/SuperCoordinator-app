package models.base;

import failures.newVersion.failure_occurrence;
import models.sensor_actuator;
import org.apache.commons.math3.util.Pair;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

public class SFEI implements Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeObject(sfeiType);
        out.writeObject(inSensor);
        out.writeObject(outSensor);
        out.writeLong(minOperationTime);
        out.writeObject(partsATM);
        out.writeInt(nPiecesMoved);
        out.writeObject(dayOfBirth);
        out.writeObject(dayOfLastMaintenance);
        out.writeBoolean(line_start);
        out.writeBoolean(line_end);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = (String) in.readObject();
        this.sfeiType = (SFEI_type) in.readObject();
        this.inSensor = (sensor_actuator) in.readObject();
        this.outSensor = (sensor_actuator) in.readObject();
        this.minOperationTime = in.readLong();

        this.partsATM = (TreeSet<part>) in.readObject();
        this.nPiecesMoved = in.readInt();
        this.dayOfBirth = (Instant) in.readObject();
        this.dayOfLastMaintenance = (Instant) in.readObject();
        this.line_start = in.readBoolean();
        this.line_end = in.readBoolean();

        this.failuresHistory = new TreeMap<>();
        this.breakdownHistory = new ArrayList<>();

    }

    public enum SFEI_type {
        CONVEYOR,
        MACHINE,
        TRANSPORT
    }

    private String name;
    private SFEI_type sfeiType;
    private sensor_actuator inSensor;
    private sensor_actuator outSensor;
    private long minOperationTime;
    private TreeSet<part> partsATM;
    private int nPiecesMoved;
    private Instant dayOfBirth;
    private Instant dayOfLastMaintenance;

    private TreeMap<Integer, failure_occurrence> failuresHistory;

    private ArrayList<Pair<Integer, Instant>> breakdownHistory;

    private boolean line_start;
    private boolean line_end;

    public SFEI() {
    }

    public SFEI(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance,
                boolean line_start, boolean line_end) {
        this.name = name;
        this.sfeiType = sfeiType;
        this.inSensor = inSensor;
        this.outSensor = outSensor;
        this.dayOfBirth = dayOfBirth;
        this.dayOfLastMaintenance = dayOfLastMaintenance;

        this.nPiecesMoved = 0;
        this.partsATM = new TreeSet<>(partsOrder.INSTANCE);

        this.failuresHistory = new TreeMap<>();

        this.breakdownHistory = new ArrayList<>();

        this.line_start = line_start;
        this.line_end = line_end;

    }

    enum partsOrder implements Comparator<part> {
        INSTANCE;

        public int compare(part p1, part p2) {
            return p1.getId() - p2.getId();
        }
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

    public boolean isLine_start() {
        return line_start;
    }

    public boolean isLine_end() {
        return line_end;
    }

}
