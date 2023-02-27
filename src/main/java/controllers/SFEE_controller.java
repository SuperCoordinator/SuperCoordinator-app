package controllers;

import failures.SFEE_failures;
import models.SFEE;
import models.SFEI.SFEI;
import models.SFEI.SFEI_conveyor;
import models.SFEI.SFEI_machine;
import models.sensor_actuator;
import monitor.SFEE_monitor;
import monitor.SFEM_monitor;
import monitor.setupRun;
import utils.utils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class SFEE_controller {

    private final SFEE sfee;

    private SFEE_monitor sfeeMonitor;

    private SFEE_failures sfeeFailures;

    private final viewers.SFEE viewer;
    private final utils utility;

    public SFEE_controller(SFEE sfee) {
        this.sfee = sfee;

        this.viewer = new viewers.SFEE();
        this.utility = new utils();
    }

    public void init() {
        try {
            //String[] comConfig = viewer.setupComunication(sfee.getCom().ordinal());
            String[] comConfig = {"192.168.240.1", "502", "1"};
            openCommunication(comConfig[0], Integer.parseInt(comConfig[1]), Integer.parseInt(comConfig[2]));

            //String csv_path = viewer.readIOpath();
            String csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\simulation\\Tags_CMC_Modbus.csv";
            importIO(csv_path);

            // # of SFEI to be added
            /*
            String input = viewer.nSFEI();
            for (int i = 0; i < Integer.parseInt(input); i++) {
                // 1 - Conveyor, 2 - Machine
                int sfeiType = Integer.parseInt(viewer.SFEI_type());
                String[] inputs = viewer.SFEI_params(i, sfeiType);
                if (sfeiType == 1) {
                    if (sfee.getSFEE_type().equals(SFEE.SFEE_type.SIMULATION)) {

                        SFEI_conveyor sfeiConveyor = addNewSFEI_conveyor(
                                inputs[0],
                                inputs[1],
                                inputs[2],
                                Instant.parse(inputs[3]),
                                Instant.parse(inputs[4]),
                                inputs[5],
                                inputs[6],
                                inputs[7],
                                inputs[8]);

                    } else {
                        // For now will not enter here !!!!
                        SFEI_conveyor sfeiConveyor = addNewSFEI_conveyor(
                                inputs[0],
                                inputs[1],
                                inputs[2],
                                Instant.parse(inputs[3]),
                                Instant.parse(inputs[4]),
                                inputs[5]);
                    }
                } else if (sfeiType == 2) {
                    SFEI_machine sfeiMachine = addNewSFEI_machine(
                            inputs[0],
                            inputs[1],
                            inputs[2],
                            Instant.parse(inputs[3]),
                            Instant.parse(inputs[4]),
                            inputs[5]);
                }

            }
            */

            addNewSFEI_conveyor(
                    "entry_conveyor",
                    "s_emitter",
                    "s_lids_at_entry",
                    Instant.now(),
                    Instant.now(),
                    "entry_remover",
                    "entry_emitter",
                    "s_entry_remover",
                    "s_entry_emitter");
            addNewSFEI_machine(
                    "MC1",
                    "s_lids_at_entry",
                    "s_lids_at_exit",
                    Instant.now(),
                    Instant.now(),
                    "MC1_stop");
            addNewSFEI_conveyor(
                    "exit_conveyor",
                    "s_lids_at_exit",
                    "s_remover",
                    Instant.now(),
                    Instant.now(),
                    "exit_remover",
                    "exit_emitter",
                    "s_exit_remover",
                    "s_exit_emitter");

            // SFEIs do not need controllers (??)

            autoSetSFEE_InOut();
            sfeeMonitor = new SFEE_monitor(sfee);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /* ***********************************
               SFEE Communications
    ************************************ */
    private void openCommunication(String ip, int port, int slaveID) {
        try {
            if (sfee.getCom() == SFEE.communication.MODBUS) sfee.getMb().openConnection(ip, port, slaveID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeCommunication() {
        sfee.getMb().closeConnection();
    }

    /* ***********************************
                    I/O
     ************************************ */

    public void importIO(String file_path) {
        sfee.setIo(utility.getReader().readModbusTags(file_path, false));
    }

    public void printAllIO() {
        viewer.listAllIO(sfee.getIo());
    }

    /* ***********************************
                    SFEI
    ************************************ */
    public SFEI_conveyor addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String aRemover, String aEmitter, String sRemover, String sEmitter) {

        sensor_actuator[] vector = new sensor_actuator[4];
        vector[0] = sfee.getIObyName(aRemover);
        vector[1] = sfee.getIObyName(aEmitter);
        vector[2] = sfee.getIObyName(sRemover);
        vector[3] = sfee.getIObyName(sEmitter);
        SFEI_conveyor newObj = new SFEI_conveyor(name, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, vector);
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

        return newObj;
    }

    public SFEI_conveyor addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String conveyorMotor) {
        SFEI_conveyor newObj = new SFEI_conveyor(name, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, sfee.getIObyName(conveyorMotor));
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);
        return newObj;
    }

    public SFEI_machine addNewSFEI_machine(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String aStop) {
        SFEI_machine newObj = new SFEI_machine(name, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, sfee.getIObyName(aStop));
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);
        return newObj;

    }


    // Change to PRIVATE in future
    private void autoSetSFEE_InOut() {
        this.sfee.setInSensor(sfee.getSFEIs().get(0).getInSensor());
        this.sfee.setOutSensor(sfee.getSFEIs().get(sfee.getSFEIs().size() - 1).getOutSensor());
    }

    /* First Run in order to get the minimum working time for each element */
    public void launchSetup() {

        try {
            List<Callable<Long>> taskList = new ArrayList<>();
            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                taskList.add(new setupRun(sfei.getValue(), sfee.getMb()));
            }
            ExecutorService executorService = Executors.newFixedThreadPool(sfee.getSFEIs().size());
            System.out.print("Press ENTER to start simulation of " + sfee.getName());
            Scanner in = new Scanner(System.in);
            in.nextLine();

            launchSimulation();
            List<Future<Long>> futures = executorService.invokeAll(taskList);

            for (Future<Long> future : futures) {
                // Waits for all tasks to return
                future.get();

            }
            executorService.shutdown();

            stopSimulation();
            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                System.out.println("SFEI " + sfei.getValue().getName() + " minTime(s): " + sfei.getValue().getMinOperationTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startMonitoring() {

        // Launch monitor thread
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(sfeeMonitor, 0, 50, TimeUnit.MILLISECONDS);

    }

    public void launchSimulation() {
        sfee.getMb().writeState(sfee.getIObyName("FACTORY I/O (Run)"), "1");
    }

    public void stopSimulation() {
        sfee.getMb().writeState(sfee.getIObyName("FACTORY I/O (Run)"), "0");
    }


}
