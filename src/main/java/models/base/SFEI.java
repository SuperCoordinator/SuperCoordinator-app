package models.base;

import failures.newVersion.failure_occurrence;
import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;
import models.SFEx_particular.SFEI_transport;
import models.sensor_actuator;
import org.apache.commons.math3.util.Pair;
import utils.InstantAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({SFEI_conveyor.class, SFEI_machine.class, SFEI_transport.class})
//@XmlType(propOrder = {"name", "type", "min_operation_time", "manufacturing_date", "last_maintenance", "isLine_start", "isLine_end", "input_sensor", "output_sensor"})
public class SFEI implements Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeObject(sfeiType);
        out.writeObject(inSensor);
        out.writeObject(outSensor);
        out.writeLong(minOperationTime);
/*        out.writeObject(partsATM);
        out.writeInt(nPiecesMoved);*/
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

//        this.partsATM = (TreeSet<part>) in.readObject();
//        this.nPiecesMoved = in.readInt();
        this.dayOfBirth = (Instant) in.readObject();
        this.dayOfLastMaintenance = (Instant) in.readObject();
        this.line_start = in.readBoolean();
        this.line_end = in.readBoolean();

/*        this.partsATM = new TreeSet<>(partsOrder.INSTANCE);
        this.failuresHistory = new TreeMap<>();
        this.breakdownHistory = new ArrayList<>();*/

    }

    public enum SFEI_type {
        CONVEYOR,
        MACHINE,
        TRANSPORT
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

    private boolean supportsFailures;

    private TreeMap<Integer, failure_occurrence> failuresHistory = new TreeMap<>();

    private ArrayList<Pair<Integer, Instant>> breakdownHistory = new ArrayList<>();
    @XmlAttribute
    private boolean line_start;
    @XmlAttribute
    private boolean line_end;

    public SFEI() {
    }

    public SFEI(String name, SFEI_type sfeiType, sensor_actuator inSensor, sensor_actuator outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance,
                boolean supportsFailures, boolean line_start, boolean line_end) {
        this.name = name;
        this.sfeiType = sfeiType;
        this.inSensor = inSensor;
        this.outSensor = outSensor;
        this.dayOfBirth = dayOfBirth;
        this.dayOfLastMaintenance = dayOfLastMaintenance;
        this.supportsFailures = supportsFailures;

        this.line_start = line_start;
        this.line_end = line_end;

    }

    enum partsOrder implements Comparator<part> {
        INSTANCE;

        public int compare(part p1, part p2) {
            return p1.getId() - p2.getId();
        }
    }

    //    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    //    @XmlAttribute(name = "type")
    public SFEI_type getSfeiType() {
        return sfeiType;
    }

    //    @XmlElement(name = "input_sensor")
    public sensor_actuator getInSensor() {
        return inSensor;
    }

    //    @XmlElement(name = "output_sensor")
    public sensor_actuator getOutSensor() {
        return outSensor;
    }

    //    @XmlAttribute(name = "min_operation_time")
    public long getMinOperationTime() {
        return minOperationTime;
    }

    public void setMinOperationTime(long minOperationTime) {
        this.minOperationTime = minOperationTime;
    }

    //    @XmlTransient
    public synchronized TreeSet<part> getPartsATM() {
        return partsATM;
    }

    public synchronized void addNewPartATM(part partATM) {
        this.partsATM.add(partATM);
    }

    //    @XmlTransient
    public int getnPiecesMoved() {
        return nPiecesMoved;
    }

    public void setnPiecesMoved(int nPiecesMoved) {
        this.nPiecesMoved = nPiecesMoved;
    }

    //    @XmlAttribute(name = "manufacturing_date", required = true)
//    @XmlJavaTypeAdapter(InstantAdapter.class)
    public Instant getDayOfBirth() {
        return dayOfBirth;
    }

    //    @XmlAttribute(name = "last_maintenance", required = true)
//    @XmlJavaTypeAdapter(InstantAdapter.class)
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

    //    @XmlAttribute(name = "isLine_start")
    public boolean isLine_start() {
        return line_start;
    }

    //    @XmlAttribute(name = "isLine_end")
    public boolean isLine_end() {
        return line_end;
    }

}
