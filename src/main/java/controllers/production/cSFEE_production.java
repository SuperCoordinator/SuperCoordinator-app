package controllers.production;

import communication.modbus;
//import failures.oldVersion.SFEE_failures;
import failures.newVersion.SFEE_failures2;
import failures.stochasticTime;
import models.base.SFEE;
import models.base.SFEI;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import models.partDescription;
import models.sensor_actuator;
import monitor.production.SFEE_production_monitor;
import monitor.setupRun;
import utility.utils;

import javax.xml.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEE_production {
    public enum operationMode {
        NORMAL,
        PROG_FAILURES
    }

    private SFEE sfee;
    @XmlElement
    private modbus mb;
    @XmlAttribute
    private operationMode opMode;
    @XmlElement
    private SFEE_production_monitor sfeeMonitor;
    @XmlElement
    private SFEE_failures2 sfeeFailures2;

    private viewers.SFEE viewer = new viewers.SFEE();

    public cSFEE_production() {
    }

    public cSFEE_production(SFEE sfee, modbus mb) {
        this.sfee = sfee;
        this.mb = mb;
    }


    public SFEE getSFEE() {
        return sfee;
    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;
    }

    public String getSFEE_name() {
        return sfee.getName();
    }

    public void setMb(modbus mb) {
        this.mb = mb;
    }

    public SFEE_failures2 getSfeeFailures2() {
        return sfeeFailures2;
    }

    public modbus getMb() {
        return mb;
    }

    public operationMode getOpMode() {
        return opMode;
    }

    public void init(int scene) {
        try {
            String csv_path;
            switch (scene) {
                case 0, -1 -> {
                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC_connection\\simulation\\Tags_CMC-connection_Modbus.csv";
                    importIO(csv_path,true);
                }
                case 3 -> {
                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC2_con_individual\\simulation\\Tags_CMC1-connection_Modbus.csv";
                    importIO(csv_path,true);
                }
                case 4 -> {
                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\CMC2_con_individual\\simulation\\Tags_CMC2-connection_Modbus.csv";
                    importIO(csv_path,true);
                }
                case 5, 6, 7 -> {
                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\sorting_station\\simulation\\Tags_sorting_station_Modbus.csv";
                    importIO(csv_path,true);
                }
                case 8 -> {
                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\SS_3CMC\\simulation\\Tags_sorting_station_Modbus.csv";
                    importIO(csv_path,true);
                }
                case 9, 10, 11 -> {
                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\SS_3CMC\\simulation\\Tags_3CMC_Modbus.csv";
                    importIO(csv_path,true);
                }
                case 12 -> {
                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\MC_Staudinger\\simulation\\Tags_MC_Staudinger.csv";
                    importIO(csv_path,true);
                }
                case 13 -> {
                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\WH_SS_WH\\simulation\\Tags_sorting_station_Modbus.csv";
                    importIO(csv_path,true);
                }
                default -> {
                    csv_path = viewer.readIOpath();
                    importIO(csv_path,true);
                }
            }
            sfee.setIO_path(csv_path);

            String mode = viewer.opMode();
            //String mode = "2";
            if (Integer.parseInt(mode) == 1) {
                opMode = operationMode.NORMAL;
            } else {
                opMode = operationMode.PROG_FAILURES;
            }


            if (scene == 3 || scene == 0) {
                addNewSFEI_conveyor(
                        "entry_conveyor",
                        "s_emitter",
                        "s_lids_at_entry",
                        Instant.now(),
                        Instant.now(),
                        true,
                        true,
                        "entry_remover",
                        "entry_emitter",
                        "s_entry_remover",
                        "s_entry_emitter",
                        "entry_conveyor",
                        true,
                        false);
                addNewSFEI_machine(
                        "MC1",
                        partDescription.form.LID,
                        "s_lids_at_entry",
                        "s_lids_at_exit",
                        Instant.now(),
                        Instant.now(),
                        true,
                        true,
                        "MC1_produce",
                        "MC1_opened",
                        "MC1_stop",
                        false,
                        false);
                addNewSFEI_conveyor(
                        "exit_conveyor",
                        "s_lids_at_exit",
                        "s_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        true,
                        "exit_remover",
                        "exit_emitter",
                        "s_exit_remover",
                        "s_exit_emitter",
                        "exit_conveyor",
                        false,
                        false);
            }
            if (scene == -1) {
                addNewSFEI_conveyor(
                        "entry2_conveyor",
                        "s_emitter2",
                        "s_lids_at_entry2",
                        Instant.now(),
                        Instant.now(),
                        true,
                        true,
                        "entry_remover2",
                        "entry_emitter2",
                        "s_entry_remover2",
                        "s_entry_emitter2",
                        "entry_conveyor2",
                        false,
                        true);
            }


            if (scene == 4) {
                addNewSFEI_conveyor(
                        "entry_conveyor",
                        "s_emitter",
                        "s_lids_at_entry",
                        Instant.now(),
                        Instant.now(),
                        true,
                        true,
                        "entry_remover",
                        "entry_emitter",
                        "s_entry_remover",
                        "s_entry_emitter",
                        "entry_conveyor",
                        false,
                        false);
                addNewSFEI_machine(
                        "MC1",
                        partDescription.form.LID,
                        "s_lids_at_entry",
                        "s_lids_at_exit",
                        Instant.now(),
                        Instant.now(),
                        true,
                        true,
                        "MC1_produce",
                        "MC1_opened",
                        "MC1_stop",
                        false,
                        false);
                addNewSFEI_conveyor(
                        "exit_conveyor",
                        "s_lids_at_exit",
                        "s_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        true,
                        "exit_remover",
                        "exit_emitter",
                        "s_exit_remover",
                        "s_exit_emitter",
                        "exit_conveyor",
                        false,
                        true);
            }
            if (scene == 5) {
                addNewSFEI_conveyor(
                        "metal_entry",
                        "s_metal",
                        "s_metal_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        false,
                        "none",
                        "none",
                        "none",
                        "none",
                        "metal_conveyor",
                        false,
                        false);
            }
            if (scene == 6) {
                addNewSFEI_conveyor(
                        "green_entry",
                        "s_green",
                        "s_green_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        false,
                        "none",
                        "none",
                        "none",
                        "none",
                        "green_conveyor",
                        false,
                        false);
            }
            if (scene == 7) {
                addNewSFEI_conveyor(
                        "blue_entry",
                        "s_blue",
                        "s_blue_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        false,
                        "",
                        "",
                        "",
                        "",
                        "blue_conveyor",
                        false,
                        false);
            }
            if (scene == 8 || scene == 13) {
                addNewSFEI_conveyor(
                        "parts_entry",
                        "s_emitter",
                        "s_exit",
                        Instant.now(),
                        Instant.now(),
                        true,
                        true,
                        "none",
                        "none",
                        "none",
                        "none",
                        "exit_conveyor",
                        true,
                        false);

                addNewSFEI_conveyor(
                        "metal_entry",
                        "s_metal",
                        "s_metal_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        false,
                        "none",
                        "none",
                        "none",
                        "none",
                        "metal_conveyor",
                        false,
                        false);
                addNewSFEI_conveyor(
                        "green_entry",
                        "s_green",
                        "s_green_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        false,
                        "none",
                        "none",
                        "none",
                        "none",
                        "green_conveyor",
                        false,
                        false);
                addNewSFEI_conveyor(
                        "blue_entry",
                        "s_blue",
                        "s_blue_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        false,
                        "none",
                        "none",
                        "none",
                        "none",
                        "blue_conveyor",
                        false,
                        false);
                addNewSFEI_conveyor(
                        "parts_exit",
                        "s_faulty",
                        "s_faulty_remover",
                        Instant.now(),
                        Instant.now(),
                        true,
                        false,
                        "none",
                        "none",
                        "none",
                        "none",
                        "faulty_conveyor",
                        false,
                        false);
            }

            switch (scene) {
                case 9, 10, 11 -> {
                    int index = scene - 9;
                    add_CMC_block(index);
                }
            }

            if (scene == 12) {
                addNewSFEI_machine("MCS_MC1",
                        partDescription.form.UNKNOWN,
                        "s_pusher_forward",
                        "s_conveyor2",
                        Instant.now(),
                        Instant.now(),
                        false,
                        false,
                        "none",
                        "none",
                        "machine1_tool",
                        true,
                        false);
                addNewSFEI_machine("MCS_MC2",
                        partDescription.form.UNKNOWN,
                        "s_conveyor2",
                        "sw_endLine",
                        Instant.now(),
                        Instant.now(),
                        false,
                        false,
                        "none",
                        "none",
                        "machine2_tool",
                        false,
                        true);
            }


//            autoSetSFEE_InOut();
            autoSetSFEE_function();

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


        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }


    private void add_CMC_block(int index) {
        addNewSFEI_conveyor(
                "EntryConveyor_" + index,
                "s_E" + index,
                "s_entryMC" + index,
                Instant.now(),
                Instant.now(),
                true,
                true,
                "entry_R" + index,
                "entry_E" + index,
                "s_entry_R" + index,
                "s_entry_E" + index,
                "entry_C" + index,
                false,
                false);
        addNewSFEI_machine(
                "MachineCenter_" + index,
                partDescription.form.LID,
                "s_entryMC" + index,
                "s_exitMC" + index,
                Instant.now(),
                Instant.now(),
                true,
                true,
                "MC" + index + "_produce",
                "MC" + index + "_opened",
                "MC" + index + "_stop",
                false,
                false);
        addNewSFEI_conveyor(
                "ExitConveyor_" + index,
                "s_exitMC" + index,
                "s_R" + index,
                Instant.now(),
                Instant.now(),
                true,
                true,
                "exit_R" + index,
                "exit_E" + index,
                "s_exit_R" + index,
                "s_exit_E" + index,
                "exit_C" + index,
                false,
                false);
    }

    private void addSFEIS_manually() {
        String input = viewer.nSFEI();
        for (int i = 0; i < Integer.parseInt(input); i++) {
            // 1 - Conveyor, 2 - Machine
            int sfeiType = Integer.parseInt(viewer.SFEI_type());
            String[] inputs = viewer.SFEI_params(i, sfeiType, viewer.isSFEI_simulated());
            if (sfeiType == 1) {
                if (sfee.getSFEE_type().equals(SFEE.SFEE_environment.SIMULATION)) {

/*
                    addNewSFEI_conveyor(
                            inputs[0],
                            inputs[1],
                            inputs[2],
                            Instant.parse(inputs[3]),
                            Instant.parse(inputs[4]),
                            inputs[5],
                            inputs[6],
                            inputs[7],
                            inputs[8],
                            inputs[9],
                            inputs[10].contains("y"),
                            inputs[11].contains("y"));
*/

                } else {
/*                    addNewSFEI_conveyor(
                            inputs[0],
                            inputs[1],
                            inputs[2],
                            Instant.parse(inputs[3]),
                            Instant.parse(inputs[4]),
                            inputs[5],
                            inputs[6].contains("y"),
                            inputs[7].contains("y"));*/
                }
            } else if (sfeiType == 2) {
/*                addNewSFEI_machine(
                        inputs[0],
                        inputs[1],
                        inputs[2],
                        Instant.parse(inputs[3]),
                        Instant.parse(inputs[4]),
                        inputs[5]);*/
            }

        }
    }

    /* ***********************************
               SFEE Communications
    ************************************ */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void openCommunication() {
        try {
            if (!mb.isConfigured()) {
                if (sfee.getCom() == SFEE.communicationOption.MODBUS) {
                    mb.openConnection(/*ip, port, slaveID,*/ sfee.getIo());
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

    public void importIO(String file_path,boolean dbg) {
        sfee.setIo(utils.getInstance().getReader().readModbusTags(file_path, dbg));
//        printAllIO();
    }

    public void printAllIO() {
        viewer.listAllIO(sfee.getIo());
    }

    /* ***********************************
                    SFEI
    ************************************ */
    public void addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, boolean isSimulation, boolean supportFailures, String aRemover, String aEmitter, String sRemover, String sEmitter, String aConveyorMotor, boolean is_line_start, boolean is_line_end) {

        sensor_actuator[] vector = new sensor_actuator[5];
        vector[0] = sfee.getIObyName(aRemover);
        vector[1] = sfee.getIObyName(aEmitter);
        vector[2] = sfee.getIObyName(sRemover);
        vector[3] = sfee.getIObyName(sEmitter);
        vector[4] = sfee.getIObyName(aConveyorMotor);
        SFEI_conveyor newObj = new SFEI_conveyor(
                name,
                SFEI.SFEI_type.CONVEYOR,
                sfee.getIObyName(inSensor),
                sfee.getIObyName(outSensor),
                dayOfBirth, dayOfLastMaintenance,
                isSimulation, supportFailures, is_line_start, is_line_end,
                vector);
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

    }

/*    public SFEI_conveyor addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, String conveyorMotor, boolean is_line_start, boolean is_line_end) {
        SFEI_conveyor newObj = new SFEI_conveyor(name, SFEI.SFEI_type.CONVEYOR, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, is_line_start, is_line_end, sfee.getIObyName(conveyorMotor));
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);
        return newObj;
    }*/

    public void addNewSFEI_machine(String name, partDescription.form partForm, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, boolean isSimulation, boolean supportsFailures, String aProduce, String sDoor, String aStop, boolean is_line_start, boolean is_line_end) {
        sensor_actuator[] vector = new sensor_actuator[3];
        vector[0] = sfee.getIObyName(aProduce);
        vector[1] = sfee.getIObyName(sDoor);
        vector[2] = sfee.getIObyName(aStop);
        SFEI_machine newObj = new SFEI_machine(name, SFEI.SFEI_type.MACHINE, partForm, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, isSimulation, supportsFailures, is_line_start, is_line_end, vector);
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

    }

    private void autoSetSFEE_InOut() {

        this.sfee.setInSensor(sfee.getSFEIs().get(0).getInSensor());
        this.sfee.setOutSensor(sfee.getSFEIs().get(sfee.getSFEIs().size() - 1).getOutSensor());
    }

    private void autoSetSFEE_function() {

        // Detect if it is the starting of the line, so it is the Sorting Station case!
//        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
//            if (entry.getValue().isLine_start() && sfee.getSFEE_type().equals(SFEE.SFEE_environment.SIMULATION)) {
//                sfee.setSFEE_function(SFEE.SFEE_role.SORTING_STATION);
//                break;
//            }
//        }
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

    public void init_after_XML_load() {

        // Load IO to the SFEE (from the path)
        importIO(sfee.getIO_path(),false);

        if (opMode.equals(operationMode.PROG_FAILURES))
            sfeeFailures2.setSfee(sfee);
        sfeeMonitor.setSfee(sfee);
    }


    public void loop() {
        try {

            List<Object> discreteInputsState = new ArrayList<>(mb.readDiscreteInputs());
            List<Object> inputRegsValue = new ArrayList<>(mb.readInputRegisters());

            List<Object> actuatorsState = new ArrayList<>(mb.readCoils());
            actuatorsState = new ArrayList<>(Collections.nCopies(actuatorsState.size(), -1));

            sfeeMonitor.loop(discreteInputsState, inputRegsValue, actuatorsState);

            if (opMode.equals(operationMode.PROG_FAILURES)) {

                // The function mb.readCoils() is only to initialize the list elements with a given size
                ArrayList<List<Object>> inputs = new ArrayList<>();
                inputs.add(discreteInputsState);

                ArrayList<List<Object>> outputs = new ArrayList<>();
                outputs.add(actuatorsState);

                sfeeFailures2.loop(inputs, outputs);
            }
            mb.writeCoils(actuatorsState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchSimulation() {
        if (sfee.getSFEE_type().equals(SFEE.SFEE_environment.SIMULATION))
            mb.writeSingleCoil(sfee.getIObyName("FACTORY I/O (Run)").getBit_offset(), 1);
        else
            mb.writeSingleCoil(sfee.getIObyName("start_module").getBit_offset(), 1);
    }

    public void stopSimulation() {
        mb.writeSingleCoil(sfee.getIObyName("FACTORY I/O (Run)").getBit_offset(), 0);
    }

    private Long[] getSFEEOperationTime() {
        Long[] array = new Long[sfee.getSFEIs().size()];

        for (int i = 0; i < sfee.getSFEIs().size(); i++) {
            array[i] = sfee.getSFEIbyIndex(i).getMinOperationTime();
        }
        return array;
    }


}
