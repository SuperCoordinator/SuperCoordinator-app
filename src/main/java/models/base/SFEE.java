package models.base;

import models.sensor_actuator;

import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEE {

    public enum SFEE_environment {
        SIMULATION, REAL
    }

    @XmlAttribute
    private SFEE_environment SFEE_environment;

    public enum SFEE_role {
        PRODUCTION,
        SORTING_STATION,
        TRANSPORT,
        WAREHOUSE
    }

    @XmlAttribute
    private SFEE_role sfeeRole;

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
    private String IO_path;
//    @XmlElement
    private TreeMap<Integer, sensor_actuator> io;
    @XmlElement
    private TreeMap<Integer, SFEI> SFEIs;

    public SFEE() {
    }

    public SFEE(String name, SFEE_environment sfee_environment, SFEE_role sfeeFunction, communicationOption com) {
        this.name = name;
        this.SFEE_environment = sfee_environment;
        this.sfeeRole = sfeeFunction;
        this.com = com;
        this.io = new TreeMap<>((Comparator<Integer> & Serializable) Integer::compareTo);
        this.SFEIs = new TreeMap<>((Comparator<Integer> & Serializable) Integer::compareTo);
    }

    public String getName() {
        return name;
    }

    public SFEE_environment getSFEE_environment() {
        return SFEE_environment;
    }

    public SFEE_role getSFEE_role() {
        return sfeeRole;
    }


    public communicationOption getCom() {
        return com;
    }

    public String getIO_path() {
        return IO_path;
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

    public void setIO_path(String IO_path) {
        this.IO_path = IO_path;
    }

    public void setIo(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public sensor_actuator getIObyName(String name) {
        try {
            if (name.equalsIgnoreCase("none"))
                return null;

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
