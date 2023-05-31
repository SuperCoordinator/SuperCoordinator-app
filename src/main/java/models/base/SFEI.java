package models.base;

import failures.evaluations.failure_occurrence;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import models.SFEx.SFEI_pusher;
import models.SFEx.SFEI_transport;
import models.sensor_actuator;
import org.apache.commons.math3.util.Pair;
import utility.InstantAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({SFEI_conveyor.class, SFEI_machine.class, SFEI_transport.class, SFEI_pusher.class})
public class SFEI {

    public enum SFEI_type {
        CONVEYOR,
        PUSHER,
        MACHINE,
        TRANSPORT,
        WAREHOUSE
    }

    @XmlAttribute
    private String name;
    @XmlAttribute
    private SFEI_type sfeiType;
    @XmlElement
    private sensor_actuator inSensor;
    @XmlElement
    private sensor_actuator outSensor;
    @XmlAttribute
    private long minOperationTime;
    private TreeSet<part> partsATM = new TreeSet<>(partsOrder.INSTANCE);
    private int nPiecesMoved = 0;

    @XmlAttribute(name = "manufacturing_date", required = true)
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant dayOfBirth;
    @XmlAttribute(name = "last_maintenance", required = true)
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant dayOfLastMaintenance;
    @XmlAttribute
    private boolean simulation;
    @XmlAttribute
    private boolean supportsFailures;
    @XmlAttribute
    private boolean line_start;
    @XmlAttribute
    private boolean line_end;
    private final TreeMap<Integer, failure_occurrence> failuresHistory = new TreeMap<>();

    public SFEI() {
    }

    public SFEI(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor,
                Instant dayOfBirth, Instant dayOfLastMaintenance, int minOperationTime,
                boolean simulation, boolean supportsFailures, boolean line_start, boolean line_end) {
        this.name = name;
        this.sfeiType = sfeiType;
        this.inSensor = inSensor;
        this.outSensor = outSensor;
        this.dayOfBirth = dayOfBirth;
        this.dayOfLastMaintenance = dayOfLastMaintenance;
        this.simulation = simulation;
        this.supportsFailures = supportsFailures;
        this.minOperationTime = minOperationTime;
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

    public void setPartsATM(TreeSet<part> partsATM) {
        this.partsATM = partsATM;
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

    public void addNewFailureOccurrence(failure_occurrence failureOccurrence) {
        failuresHistory.put(failuresHistory.size(), failureOccurrence);
    }

    public boolean isSimulation() {
        return simulation;
    }

    public boolean isSupportsFailures() {
        return supportsFailures;
    }

    public boolean isLine_start() {
        return line_start;
    }

    public void setLine_start(boolean line_start) {
        this.line_start = line_start;
    }

    public boolean isLine_end() {
        return line_end;
    }

    public void setLine_end(boolean line_end) {
        this.line_end = line_end;
    }
}
