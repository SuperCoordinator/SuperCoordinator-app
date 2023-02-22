package models;

import communication.modbus;
import monitor.time.conveyor;
import utils.utils;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class eduBlock {

    public enum communication {
        MODBUS, OPC_UA
    }

    public enum blockType {
        SIMULATION, REAL
    }

    private final communication com;

    private final blockType blockType;
    private String name;
    private modbus mb;
    private String io_file_path;
    TreeMap<String, sensor_actuator> io;
    utils utility;
    private final viewers.eduBlock viewer;

    public eduBlock(String name, blockType blockType, communication com) {

        this.name = name;
        this.com = com;
        this.blockType = blockType;
        this.viewer = new viewers.eduBlock();

        io = new TreeMap<>();

        this.monitorTimeConveyors = new ArrayList<>();
        this.failureConveyors = new ArrayList<>();

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


    /*********************************
     * Monitoring Part
     *********************************/
    private ArrayList<monitor.time.conveyor> monitorTimeConveyors;
    private ArrayList<failures.conveyor> failureConveyors;

    public ArrayList<conveyor> getMonitorTimeConveyors() {
        return monitorTimeConveyors;
    }

    public void addMonitorTimeConveyor(conveyor monitorTimeConveyor) {
        monitorTimeConveyor.setMb(mb);
        this.monitorTimeConveyors.add(monitorTimeConveyor);
    }

    /*********************************
     * Failures Part
     *********************************/
    public ArrayList<failures.conveyor> getFailureConveyors() {
        return failureConveyors;
    }

    public void addFailureConveyor(failures.conveyor failureConveyor) {
        failureConveyor.setMB(mb);
        this.failureConveyors.add(failureConveyor);
    }


    public void startBlock() {

        /*
         * Launch monitoring threads
         * Launch failure threads
         * Launch connectivity threads
         */
        try {
            if (monitorTimeConveyors.size() > 0) {

                ScheduledExecutorService monitorPoolExecutor = Executors.newScheduledThreadPool(monitorTimeConveyors.size());

                for (conveyor currConveyor : monitorTimeConveyors) {
                    monitorPoolExecutor.scheduleAtFixedRate(currConveyor, 0, 100, TimeUnit.MILLISECONDS);
                }
            }

            if (failureConveyors.size() > 0) {

                ScheduledExecutorService failurePoolExecutor = Executors.newScheduledThreadPool(failureConveyors.size());

                for (failures.conveyor currConveyor : failureConveyors) {
                    failurePoolExecutor.scheduleAtFixedRate(currConveyor, 0, 100, TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void printAllIO() {
        viewer.listAllIO(io);
    }

}
