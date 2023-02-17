package models;

import communication.modbus;
import utils.utils;

import java.util.TreeMap;

public class eduBlock {

    public enum communication {
        MODBUS, OPC_UA
    }

    private final communication com;
    private String name;

    private modbus mb;
    private String io_file_path;
    TreeMap<String, sensor_actuator> io;

    utils utility;

    private final viewers.eduBlock viewer;

    public eduBlock(String name, communication com) {

        this.name = name;
        this.com = com;
        this.viewer = new viewers.eduBlock();

        io = new TreeMap<>();

        this.utility = new utils();
        if (com == communication.MODBUS) mb = new modbus();
        else if (com == communication.OPC_UA) {
            // something for the future
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public modbus getMb() {
        return mb;
    }

    public void openCommunication(String ip, int port, int slaveID) {
        try {
            if (com == communication.MODBUS) mb.openConnection(ip, port, slaveID, io);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeCommunication() {
        mb.closeConnection();
    }

    public void importIO(String file_path) {
        utility.getReader().readModbusTags(file_path, io, false);
    }

    public void printAllIO() {
        viewer.listAllIO(io);
    }

}
