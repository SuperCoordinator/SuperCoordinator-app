package models;

import models.SFEI.SFEI;
import communication.modbus;

import java.util.*;

public class SFEE {

    public enum communicationOption {
        MODBUS, OPC_UA
    }

    public enum SFEE_type {
        SIMULATION, REAL
    }

    private final String name;
    private final communicationOption com;

    private final SFEE_type SFEE_type;

    private sensor_actuator inSensor;
    private sensor_actuator outSensor;
    private TreeMap<String, sensor_actuator> io;
    private final modbus mb;
    private final TreeMap<Integer, SFEI> SFEIs;


    public SFEE(String name, communicationOption com, modbus mb, SFEE.SFEE_type SFEE_type) {
        this.name = name;
        this.com = com;
        this.mb = mb;
        this.SFEE_type = SFEE_type;
        this.io = new TreeMap<>();
        this.SFEIs = new TreeMap<>();
    }

    public String getName() {
        return name;
    }

    public communicationOption getCom() {
        return com;
    }

    public SFEE.SFEE_type getSFEE_type() {
        return SFEE_type;
    }

    public sensor_actuator getInSensor() {
        return inSensor;
    }

    public sensor_actuator getOutSensor() {
        return outSensor;
    }

    public modbus getMb() {
        return mb;
    }

    public TreeMap<String, sensor_actuator> getIo() {
        return io;
    }

    public void setIo(TreeMap<String, sensor_actuator> io) {
        this.io = io;
    }

    public sensor_actuator getIObyName(String name) {

        try {
            for (Map.Entry<String, sensor_actuator> entry : io.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) return entry.getValue();
            }
            throw new Exception("IO with name" + name + " not found!");
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
