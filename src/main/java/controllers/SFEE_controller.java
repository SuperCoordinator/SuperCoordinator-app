package controllers;

import communication.modbus;
import failures.SFEE_failures;
import models.SFEE;
import models.SFEI.SFEI;
import models.SFEI.SFEI_conveyor;
import models.SFEI.SFEI_machine;
import models.sensor_actuator;
import monitor.SFEE_monitor;
import monitor.setupRun;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class SFEE_controller {

    public enum communicationOption {
        MODBUS, OPC_UA
    }

    private final communicationOption com;

    private final modbus mb;

    public enum operationMode {
        NORMAL,
        PROG_FAILURES
    }

    private final SFEE sfee;
    private final int temp;

    private SFEE_monitor sfeeMonitor;

    private operationMode opMode;
    private SFEE_failures sfeeFailures;

    private final viewers.SFEE viewer;
    private final utils utility;

    public SFEE_controller(SFEE sfee, communicationOption communicationOpt, modbus mb, int temp) {
        this.sfee = sfee;
        this.com = communicationOpt;
        this.mb = mb;
        this.temp = temp;

        this.viewer = new viewers.SFEE();
        this.utility = new utils();
    }

    public modbus getMb() {
        return mb;
    }


    public void init(String[] comConfig) {
        try {


            //String csv_path = viewer.readIOpath();

//            String csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC\\simulation\\Tags_CMC_Modbus.csv";
            String csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC2\\simulation\\Tags_2CMC_Modbus.csv";
            importIO(csv_path,2);

            // Initialization of the modbus connection in case of not connected already
            openCommunication(comConfig[0], Integer.parseInt(comConfig[1]), Integer.parseInt(comConfig[2]), sfee.getIo());

            String mode = viewer.opMode();
            //String mode = "2";
            if (Integer.parseInt(mode) == 1) {
                opMode = operationMode.NORMAL;
            } else {
                opMode = operationMode.PROG_FAILURES;
            }

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

            if (temp == 0) {
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
                        "MC1_opened",
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
            } else if (temp == 1) {
                addNewSFEI_conveyor(
                        "entry_conveyor2",
                        "s_emitter2",
                        "s_lids_at_entry2",
                        Instant.now(),
                        Instant.now(),
                        "entry_remover2",
                        "entry_emitter2",
                        "s_entry_remover2",
                        "s_entry_emitter2");
                addNewSFEI_machine(
                        "MC2",
                        "s_lids_at_entry2",
                        "s_lids_at_exit2",
                        Instant.now(),
                        Instant.now(),
                        "MC2_opened",
                        "MC2_stop");
                addNewSFEI_conveyor(
                        "exit_conveyor2",
                        "s_lids_at_exit2",
                        "s_remover2",
                        Instant.now(),
                        Instant.now(),
                        "exit_remover2",
                        "exit_emitter2",
                        "s_exit_remover2",
                        "s_exit_emitter2");
            }
            // SFEIs do not need controllers (??)

            autoSetSFEE_InOut();
            sfeeMonitor = new SFEE_monitor(sfee, mb.readDiscreteInputs());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /* ***********************************
               SFEE Communications
    ************************************ */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private void openCommunication(String ip, int port, int slaveID, TreeMap<Integer, sensor_actuator> io) {
        try {
            if (!mb.isConfigured()) {
                if (com == communicationOption.MODBUS) {
                    mb.openConnection(ip, port, slaveID, io);
                    scheduler.scheduleAtFixedRate(mb, 0, 50, TimeUnit.MILLISECONDS);
                }
            } else {
                scheduler.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeCommunication() {
        scheduler.shutdown();
        mb.closeConnection();
    }

    /* ***********************************
                    I/O
     ************************************ */

    public void importIO(String file_path,int scene) {
        sfee.setIo(utility.getReader().readModbusTags(file_path,scene, false));
//        printAllIO();
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
        SFEI_conveyor newObj = new SFEI_conveyor(name, SFEI.SFEI_type.CONVEYOR, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, vector);
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

        return newObj;
    }

    public SFEI_conveyor addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String conveyorMotor) {
        SFEI_conveyor newObj = new SFEI_conveyor(name, SFEI.SFEI_type.CONVEYOR, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, sfee.getIObyName(conveyorMotor));
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);
        return newObj;
    }

    public SFEI_machine addNewSFEI_machine(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String sDoor, String aStop) {
        SFEI_machine newObj = new SFEI_machine(name, SFEI.SFEI_type.MACHINE, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, sfee.getIObyName(sDoor), sfee.getIObyName(aStop));
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);
        return newObj;

    }


    // Change to PRIVATE in future
    private void autoSetSFEE_InOut() {
        this.sfee.setInSensor(sfee.getSFEIs().get(0).getInSensor());
        this.sfee.setOutSensor(sfee.getSFEIs().get(sfee.getSFEIs().size() - 1).getOutSensor());
    }

    /* First Run in order to get the minimum working stochasticTime for each element */
    public void launchSetup() {

        try {
            List<Callable<Long>> taskList = new ArrayList<>();
            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                taskList.add(new setupRun(sfei.getValue(), mb));
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

    public void initFailures() {
        if (opMode.equals(operationMode.PROG_FAILURES)) {

            // Before definition, print SFEE minimal operation stochasticTime

            Long[] opTimes = getSFEEOperationTime();
            long totalTime = 0;
            for (Long value : opTimes)
                totalTime = totalTime + value;

            System.out.println("Before start notice that SFEE " + sfee.getName() + " has " + totalTime);

            // Not needed to explicit every SFEI because de firstRun() print that!

            String[] sfeeTime = viewer.SFEEtime();

            if (Integer.parseInt(sfeeTime[0]) == 1) {
                // Stochastic Time
                sfeeFailures = new SFEE_failures(sfee, SFEE_failures.timeOptions.GAUSSIAN);
                sfeeFailures.setMean(sfeeTime[1]);
                sfeeFailures.setStd_dev(sfeeTime[2]);

            } else if (Integer.parseInt(sfeeTime[0]) == 2) {
                // Linear Time
                sfeeFailures = new SFEE_failures(sfee, SFEE_failures.timeOptions.LINEAR);
                sfeeFailures.setMean(sfeeTime[1]);
                sfeeFailures.setStd_dev("0");
            }


        }
    }


    public void loop() {

//        List<Object> sensorsState = mb.readMultipleInputs(sfee.getIo());
        List<Object> sensorsState = new ArrayList<>(mb.readDiscreteInputs());


//        String sensorsState = mb.readMultipleInputs(sfee.getIo());
//        String actuatorsStateInit = mb.readMultipleCoils(sfee.getIo());

//        System.out.println("Outputs before: " + Arrays.toString(actuatorsState.toArray()));

        sfeeMonitor.loop(sensorsState);

//        String actuatorsState = actuatorsStateInit;
        if (opMode.equals(operationMode.PROG_FAILURES)) {
//            actuatorsState = sfeeFailures.loop(sensorsState/*, mb*/, actuatorsState);
            // The function mb.readCoils() is only to initialize the list elements with a given size
            List<Object> actuatorsState = new ArrayList<>(mb.readCoils());
            sfeeFailures.loop(sensorsState, actuatorsState);
//            System.out.println("Outputs after:  " + Arrays.toString(actuatorsState.toArray()));

            // Write new state no matter if there are changes or not
            mb.writeCoils(actuatorsState);
        }


/*        //System.out.println("Outputs after:  " + actuatorsState);
        if (!actuatorsStateInit.equalsIgnoreCase(actuatorsState)) {
//            System.out.println("Outputs before: " + actuatorsStateInit);
//            System.out.println("Outputs after:  " + actuatorsState);

            mb.writeMultipleCoils(sfee.getIo(), actuatorsState);

            System.out.println("Maybe writing more times than needed!");
        }*/


    }

/*    public void startMonitoring() {
        // Launch monitor thread
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(sfeeMonitor, 0, 50, TimeUnit.MILLISECONDS);

    }

    public void startFailures() {
        if (opMode.equals(operationMode.PROG_FAILURES)) {
            // Launch monitor thread
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(sfeeFailures, 0, 50, TimeUnit.MILLISECONDS);
        }
    }*/

    public void launchSimulation() {
//        mb.writeState(sfee.getIObyName("FACTORY I/O (Run)"), "1");
        mb.writeSingleCoil(sfee.getIObyName("FACTORY I/O (Run)").bit_offset(), 1);
    }

    public void stopSimulation() {
//        mb.writeState(sfee.getIObyName("FACTORY I/O (Run)"), "0");
        mb.writeSingleCoil(sfee.getIObyName("FACTORY I/O (Run)").bit_offset(), 0);
    }

    private Long[] getSFEEOperationTime() {

        Long[] array = new Long[sfee.getSFEIs().size()];

        for (int i = 0; i < sfee.getSFEIs().size(); i++) {
            array[i] = sfee.getSFEIbyIndex(i).getMinOperationTime();
        }

        return array;
    }


}
