package models.base;

import models.sensor_actuator;

import java.util.*;

public class SFEE {

    public enum SFEE_type {
        SIMULATION, REAL
    }

    private final SFEE_type SFEE_type;

    public enum SFEE_function {
        PRODUCTION,
        MOVEMENT,
        TRANSPORT
    }

    private final SFEE_function sfeeFunction;

    public enum communicationOption {
        MODBUS, OPC_UA, MIXED
    }

    private communicationOption com;

    private String name;

    private sensor_actuator inSensor;
    private sensor_actuator outSensor;
    private TreeMap<Integer, sensor_actuator> io;

    private TreeMap<Integer, SFEI> SFEIs;

    public SFEE(String name, SFEE_type SFEE_type, SFEE_function sfeeFunction, communicationOption com) {
        this.name = name;
        this.SFEE_type = SFEE_type;
        this.sfeeFunction = sfeeFunction;
        this.com = com;
        this.io = new TreeMap<>();
        this.SFEIs = new TreeMap<>();
    }

    public communicationOption getCom() {
        return com;
    }

    public String getName() {
        return name;
    }

    public SFEE.SFEE_type getSFEE_type() {
        return SFEE_type;
    }

    public SFEE_function getSfeeFunction() {
        return sfeeFunction;
    }

    public sensor_actuator getInSensor() {
        return inSensor;
    }

    public sensor_actuator getOutSensor() {
        return outSensor;
    }

    public TreeMap<Integer, sensor_actuator> getIo() {
        return io;
    }

    public void setIo(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
    }

    public sensor_actuator getIObyName(String name) {

        try {
            for (Map.Entry<Integer, sensor_actuator> entry : io.entrySet()) {
                if (entry.getValue().name().equalsIgnoreCase(name)) return entry.getValue();
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
