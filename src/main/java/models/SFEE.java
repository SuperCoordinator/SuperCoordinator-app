package models;

import models.SFEI.SFEI;
import communication.modbus;
import models.SFEI.SFEI_conveyor;
import models.SFEI.SFEI_machine;
import monitor.setupRun;
import utils.utils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class SFEE {

    public enum communication {
        MODBUS, OPC_UA
    }

    public enum SFEE_type {
        SIMULATION, REAL
    }

    private final String name;
    private final communication com;

    private final SFEE_type SFEE_type;

    private sensor_actuator inSensor;
    private sensor_actuator outSensor;
    private final TreeMap<String, sensor_actuator> io;
    private final modbus mb;
    private final TreeMap<Integer, SFEI> SFEIs;
    private final utils utility;
    private final viewers.SFEM viewer;

    public SFEE(String name, communication com, SFEE.SFEE_type SFEE_type) {
        this.name = name;
        this.com = com;
        this.mb = new modbus();
        this.SFEE_type = SFEE_type;
        this.io = new TreeMap<>();
        this.SFEIs = new TreeMap<>();

        this.utility = new utils();
        this.viewer = new viewers.SFEM();
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

    /* ***********************************
                         SFEE Communications
                ************************************ */
    public void openCommunication(String ip, int port, int slaveID) {
        try {
            if (com == communication.MODBUS) mb.openConnection(ip, port, slaveID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeCommunication() {
        mb.closeConnection();
    }

    /* ***********************************
                    I/O
     ************************************ */

    public void importIO(String file_path) {

        utility.getReader().readModbusTags(file_path, io, false);

        printAllIO();
    }

    public void printAllIO() {
        viewer.listAllIO(io);
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

    /* ***********************************
                    SFEI
     ************************************ */
    public void addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String aRemover, String aEmitter, String sRemover, String sEmitter) {

        sensor_actuator[] vector = new sensor_actuator[4];
        vector[0] = getIObyName(aRemover);
        vector[1] = getIObyName(aEmitter);
        vector[2] = getIObyName(sRemover);
        vector[3] = getIObyName(sEmitter);
        SFEI_conveyor newObj = new SFEI_conveyor(name, getIObyName(inSensor), getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, vector);
        SFEIs.put(SFEIs.size(), newObj);

    }

    public void addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String conveyorMotor) {
        SFEIs.put(SFEIs.size(), new SFEI_conveyor(name, getIObyName(inSensor), getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, getIObyName(conveyorMotor)));
    }

    public void addNewSFEI_machine(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String aStop) {
        SFEIs.put(SFEIs.size(), new SFEI_machine(name, getIObyName(inSensor), getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, getIObyName(aStop)));

    }

    public TreeMap<Integer, SFEI> getSFEIs() {
        return SFEIs;
    }

    public SFEI getSFEIbyIndex(int index) {
        return SFEIs.get(index);
    }

    private void autoSetSFEE_InOut() {

        this.inSensor = SFEIs.get(0).getInSensor();
        this.outSensor = SFEIs.get(SFEIs.size() - 1).getOutSensor();
    }

    /* First Run in order to get the minimum working time for each element */
    public void launchSetup() {

        autoSetSFEE_InOut();

        try {
            List<Callable<Long>> taskList = new ArrayList<>();
            for (Map.Entry<Integer, SFEI> sfei : SFEIs.entrySet()) {
                taskList.add(new setupRun(sfei.getValue(), mb));
            }
            ExecutorService executorService = Executors.newFixedThreadPool(SFEIs.size());
            System.out.print("Press ENTER to start simulation");
            Scanner in = new Scanner(System.in);
            in.nextLine();

            mb.writeState(getIObyName("FACTORY I/O (Run)"), "1");
            List<Future<Long>> futures = executorService.invokeAll(taskList);

            for (Future<Long> future : futures) {
                // Waits for all tasks to return
                future.get();

            }
            executorService.shutdown();

            mb.writeState(getIObyName("FACTORY I/O (Run)"), "0");
            for (Map.Entry<Integer, SFEI> sfei : SFEIs.entrySet()) {
                System.out.println("SFEI " + sfei.getValue().getName() + " minTime(s): " + sfei.getValue().getMinOperationTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchMonitor(){

    }

}
