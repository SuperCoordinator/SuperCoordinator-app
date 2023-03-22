package controllers.production;

import communication.modbus;
//import failures.oldVersion.SFEE_failures;
import failures.newVersion.SFEE_failures2;
import failures.stochasticTime;
import models.base.SFEE;
import models.base.SFEI;
import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;
import models.partsAspect;
import models.sensor_actuator;
import monitor.SFEE_production_monitor;
import monitor.setupRun;
import utils.utils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class cSFEE_production {


    public enum operationMode {
        NORMAL,
        PROG_FAILURES
    }

    private final SFEE sfee;
    private modbus mb;
    private final int temp;
    private SFEE_production_monitor sfeeMonitor;

    private operationMode opMode;
    //    private SFEE_failures sfeeFailures;
    private SFEE_failures2 sfeeFailures2;

    private final viewers.SFEE viewer;
    private final utils utility;

    public cSFEE_production(SFEE sfee, modbus mb, int temp) {
        this.sfee = sfee;

        this.mb = mb;
        this.temp = temp;

        this.viewer = new viewers.SFEE();
        this.utility = new utils();
    }

    public String getSFEE_name() {
        return sfee.getName();
    }

    public void setMb(modbus mb) {
        this.mb = mb;
    }

    public modbus getMb() {
        return mb;
    }

    public void init(String[] comConfig) {
        try {


            //String csv_path = viewer.readIOpath();

/*            String csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC-connection\\simulation\\Tags_CMC-connection_Modbus.csv";
            importIO(csv_path, 1);*/
/*            String csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC2\\simulation\\Tags_2CMC_Modbus.csv";
            importIO(csv_path, 2);*/

/*
            String csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC2-connection\\simulation\\Tags_CMC-connection_Modbus.csv";
            importIO(csv_path, 3);
*/

            String csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC-connection\\simulation\\Tags_CMC-connection_Modbus.csv";
            importIO(csv_path, 4);
            // Initialization of the modbus connection in case of not connected already
//            openCommunication(comConfig[0], Integer.parseInt(comConfig[1]), Integer.parseInt(comConfig[2]), sfee.getIo());

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
                        "entry_conveyor",
                        "entry_remover",
                        "entry_emitter",
                        "s_entry_remover",
                        "s_entry_emitter",
                        true,
                        false);
                addNewSFEI_machine(
                        "MC1",
                        partsAspect.form.LID,
                        "s_lids_at_entry",
                        "s_lids_at_exit",
                        Instant.now(),
                        Instant.now(),
                        "MC1_produce",
                        "MC1_opened",
                        "MC1_stop", false, false);
                addNewSFEI_conveyor(
                        "exit_conveyor",
                        "s_lids_at_exit",
                        "s_remover",
                        Instant.now(),
                        Instant.now(),
                        "exit_conveyor",
                        "exit_remover",
                        "exit_emitter",
                        "s_exit_remover",
                        "s_exit_emitter", false, false);
            } /*else if (temp == 1) {
                addNewSFEI_conveyor(
                        "entry_conveyor2",
                        "s_emitter2",
                        "s_lids_at_entry2",
                        Instant.now(),
                        Instant.now(),
                        "entry_conveyor2",
                        "entry_remover2",
                        "entry_emitter2",
                        "s_entry_remover2",
                        "s_entry_emitter2");
                addNewSFEI_machine(
                        "MC2",
                        partsAspect.form.BASE,
                        "s_lids_at_entry2",
                        "s_lids_at_exit2",
                        Instant.now(),
                        Instant.now(),
                        "MC2_produce",
                        "MC2_opened",
                        "MC2_stop");
                addNewSFEI_conveyor(
                        "exit_conveyor2",
                        "s_lids_at_exit2",
                        "s_remover2",
                        Instant.now(),
                        Instant.now(),
                        "exit_conveyor2",
                        "exit_remover2",
                        "exit_emitter2",
                        "s_exit_remover2",
                        "s_exit_emitter2");
            }*/ else if (temp == 1) {
                addNewSFEI_conveyor(
                        "entry_conveyor2",
                        "s_emitter2",
                        "s_lids_at_entry2",
                        Instant.now(),
                        Instant.now(),
                        "entry_conveyor2",
                        "entry_remover2",
                        "entry_emitter2",
                        "s_entry_remover2",
                        "s_entry_emitter2", false, true);
            }
            // SFEIs do not need controllers (??)

            autoSetSFEE_InOut();
//            autoSetSFEE_function();

            // Initialize SFEE_production_monitor

            sfeeMonitor = new SFEE_production_monitor(sfee/*, mb.readDiscreteInputs()*/);

            String[] visionStr = viewer.associateVisionSensors();
//            String[] visionStr = {"y", "v_MC1_exit", "exit_conveyor"};
/*            if (temp == 1) {
                visionStr[0] = "y";
                visionStr[1] = "v_MC2_exit";
                visionStr[2] = "exit_conveyor2";
            }*/
            if (!visionStr[0].equals("no")) {
                // search for SFEI
                int sfei_id = -1;
                for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
                    if (entry.getValue().getName().equals(visionStr[2])) {
                        sfei_id = entry.getKey();
                        break;
                    }
                }
                if (sfei_id == -1)
                    throw new RuntimeException("SFEI with name '" + visionStr[2] + "' do not exist");
                TreeMap<Integer, sensor_actuator> treeMap = new TreeMap<>();
                treeMap.put(sfei_id, sfee.getIObyName(visionStr[1]));
                sfeeMonitor.setVisionSensorLocation(treeMap);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /* ***********************************
               SFEE Communications
    ************************************ */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void openCommunication(/*String ip, int port, int slaveID,*/) {
        try {
            if (!mb.isConfigured()) {
                if (sfee.getCom() == SFEE.communicationOption.MODBUS) {
                    mb.openConnection(/*ip, port, slaveID,*/ sfee.getIo());
                    scheduler.scheduleAtFixedRate(mb, 0, 50, TimeUnit.MILLISECONDS);

//                    sfeeMonitor.init_oldSensorsValues(mb.readDiscreteInputs());
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

    public void importIO(String file_path, int scene) {
        sfee.setIo(utility.getReader().readModbusTags(file_path, scene, false));
//        printAllIO();
    }

    public void printAllIO() {
        viewer.listAllIO(sfee.getIo());
    }

    /* ***********************************
                    SFEI
    ************************************ */
    public void addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String conveyorMotor, String aRemover, String aEmitter, String sRemover, String sEmitter, boolean is_line_start, boolean is_line_end) {

        sensor_actuator[] vector = new sensor_actuator[4];
        vector[0] = sfee.getIObyName(aRemover);
        vector[1] = sfee.getIObyName(aEmitter);
        vector[2] = sfee.getIObyName(sRemover);
        vector[3] = sfee.getIObyName(sEmitter);
        SFEI_conveyor newObj = new SFEI_conveyor(name, SFEI.SFEI_type.CONVEYOR, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, is_line_start, is_line_end, sfee.getIObyName(conveyorMotor), vector);
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

    }

    public SFEI_conveyor addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String conveyorMotor, boolean is_line_start, boolean is_line_end) {
        SFEI_conveyor newObj = new SFEI_conveyor(name, SFEI.SFEI_type.CONVEYOR, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, is_line_start, is_line_end, sfee.getIObyName(conveyorMotor));
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);
        return newObj;
    }

    public void addNewSFEI_machine(String name, partsAspect.form partForm, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String produce, String sDoor, String aStop, boolean is_line_start, boolean is_line_end) {
        SFEI_machine newObj = new SFEI_machine(name, SFEI.SFEI_type.MACHINE, partForm, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, is_line_start, is_line_end, sfee.getIObyName(produce), sfee.getIObyName(sDoor), sfee.getIObyName(aStop));
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

    }

    private void autoSetSFEE_InOut() {
        this.sfee.setInSensor(sfee.getSFEIs().get(0).getInSensor());
        this.sfee.setOutSensor(sfee.getSFEIs().get(sfee.getSFEIs().size() - 1).getOutSensor());
    }

/*    private void autoSetSFEE_function() {
        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
            if (sfei.getValue().getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {
                sfee.setSfeeFunction(SFEE.SFEE_function.PRODUCTION);
            }

        }
    }*/

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

            String[] sfeeTime = viewer.SFEE_stochasticTime();
//            String[] sfeeFailures_str = viewer.SFEEFailures();

            ArrayList<String[]> failures_f = viewer.SFEEFailures();

            if (sfeeTime[0].contains("gauss")) {
                // Stochastic Time
                sfeeFailures2 = new SFEE_failures2(
                        sfee,
                        stochasticTime.timeOptions.GAUSSIAN,
                        new String[]{sfeeTime[1], sfeeTime[2]},
                        failures_f);

            } else if (sfeeTime[0].contains("linear")) {
                // Linear Time
                sfeeFailures2 = new SFEE_failures2(sfee,
                        stochasticTime.timeOptions.LINEAR,
                        new String[]{sfeeTime[1], sfeeTime[2]},
                        failures_f);
            }
        }
    }


    public void loop() {

        List<Object> discreteInputsState = new ArrayList<>(mb.readDiscreteInputs());
//        System.out.println("Outputs before: " + Arrays.toString(actuatorsState.toArray()));

        List<Object> inputRegsValue = new ArrayList<>(mb.readInputRegisters());
//        System.out.println(Arrays.toString(inputRegsValue.toArray()));
        List<Object> actuatorsState = new ArrayList<>(mb.readCoils());
        actuatorsState = new ArrayList<>(Collections.nCopies(actuatorsState.size(), -1));

        sfeeMonitor.loop(discreteInputsState, inputRegsValue, actuatorsState);

        if (opMode.equals(operationMode.PROG_FAILURES)) {

            // The function mb.readCoils() is only to initialize the list elements with a given size
            ArrayList<List<Object>> inputs = new ArrayList<>();
            inputs.add(discreteInputsState);

            ArrayList<List<Object>> outputs = new ArrayList<>();
            outputs.add(actuatorsState);
//            sfeeFailures.loop(sensorsState, actuatorsState);
            sfeeFailures2.loop(inputs, outputs);
//            System.out.println("Outputs after:  " + Arrays.toString(actuatorsState.toArray()));
            // The writeCoils() function will detect and execute MB instruction only if there are changes
        }
        mb.writeCoils(actuatorsState);

    }

    public void launchSimulation() {
        mb.writeSingleCoil(sfee.getIObyName("FACTORY I/O (Run)").bit_offset(), 1);
    }

    public void stopSimulation() {
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
