package models.base;

import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;
import models.SFEx_particular.SFEI_transport;
import models.sensor_actuator;

import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEE implements Externalizable {
    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeObject(SFEE_type);
        out.writeObject(SFEE_function);
        out.writeObject(com);
        out.writeObject(io);
        out.writeObject(inSensor);
        out.writeObject(outSensor);
        out.writeObject(SFEIs);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = (String) in.readObject();
        this.SFEE_type = (SFEE_type) in.readObject();
        this.SFEE_function = (SFEE_function) in.readObject();
        this.com = (communicationOption) in.readObject();
        this.io = (TreeMap<Integer, sensor_actuator>) in.readObject();
        this.inSensor = (sensor_actuator) in.readObject();
        this.outSensor = (sensor_actuator) in.readObject();
        this.SFEIs = (TreeMap<Integer, SFEI>) in.readObject();

    }

    public enum SFEE_type {
        SIMULATION, REAL
    }

    @XmlAttribute
    private SFEE_type SFEE_type;

    public enum SFEE_function {
        PRODUCTION,
        MOVEMENT,
        TRANSPORT
    }

    @XmlAttribute
    private SFEE_function SFEE_function;

    public enum communicationOption {
        MODBUS, OPC_UA, MIXED
    }

    @XmlAttribute
    private communicationOption com;
    @XmlAttribute
    private String name;
    @XmlElement
    private sensor_actuator inSensor;
    @XmlElement
    private sensor_actuator outSensor;
    @XmlElement
    private TreeMap<Integer, sensor_actuator> io;
    @XmlElement
    private TreeMap<Integer, SFEI> SFEIs;

    public SFEE() {
    }

    public SFEE(String name, SFEE_type SFEE_type, SFEE_function sfeeFunction, communicationOption com) {
        this.name = name;
        this.SFEE_type = SFEE_type;
        this.SFEE_function = sfeeFunction;
        this.com = com;
        this.io = new TreeMap<>((Comparator<Integer> & Serializable) Integer::compareTo);
        this.SFEIs = new TreeMap<>((Comparator<Integer> & Serializable) Integer::compareTo);
    }

    public String getName() {
        return name;
    }

    public SFEE.SFEE_type getSFEE_type() {
        return SFEE_type;
    }

    public SFEE_function getSFEE_function() {
        return SFEE_function;
    }

    public communicationOption getCom() {
        return com;
    }

    public TreeMap<Integer, sensor_actuator> getIo() {
        return io;
    }

    public sensor_actuator getInSensor() {
        return inSensor;
    }


    public sensor_actuator getOutSensor() {
        return outSensor;
    }

    public void setIo(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public sensor_actuator getIObyName(String name) {

        try {
            for (Map.Entry<Integer, sensor_actuator> entry : io.entrySet()) {
                if (entry.getValue().getName().equalsIgnoreCase(name))
                    return entry.getValue();
            }
            throw new Exception("IO with name " + name + " not found!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //    @XmlElement(name = "SFEIs")
    public TreeMap<Integer, SFEI> getSFEIs() {
        return SFEIs;
    }

    public synchronized SFEI getSFEIbyIndex(int index) {
        return SFEIs.get(index);
    }

    public void setInSensor(sensor_actuator inSensor) {
        this.inSensor = inSensor;
    }

    public void setOutSensor(sensor_actuator outSensor) {
        this.outSensor = outSensor;
    }
}
