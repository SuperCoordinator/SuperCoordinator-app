package controllers.production;

import communication.modbus;
//import failures.oldVersion.SFEE_failures;
import failures.SFEE_production_failures;
import failures.stochasticTime;
import models.SFEx.SFEI_pusher;
import models.base.SFEE;
import models.base.SFEI;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import models.partDescription;
import models.sensor_actuator;
import monitor.production.SFEE_production_monitor;
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
    private SFEE_production_failures sfeeFailures;

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

    public SFEE_production_monitor getSfeeMonitor() {
        return sfeeMonitor;
    }

    public SFEE_production_failures getSfeeFailures() {
        return sfeeFailures;
    }

    public modbus getMb() {
        return mb;
    }

    public operationMode getOpMode() {
        return opMode;
    }

    public void init(/*int scene*/) {
        try {


            String csv_path = viewer.getIOpath(sfee.getName());
//            switch (scene) {
//                case 5, 6, 7 -> {
//                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\sorting_station\\simulation\\Tags_sorting_station_Modbus.csv";
//                    importIO(csv_path, true);
//                }
//                case 12 -> {
//                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\MC_Staudinger\\simulation\\Tags_MC_Staudinger.csv";
//                    importIO(csv_path, true);
//                }
//                case 13 -> {
//                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\WH_SS_WH\\simulation\\Tags_sorting_station_Modbus.csv";
//                    importIO(csv_path, true);
//                }
//                case 14, 15, 16, 17 -> {
//                    csv_path = "C:\\Users\\danie\\Documents\\GitHub\\SC-sketch\\blocks\\WH_SS_3CMC_WH\\simulation\\Tags_SS_3CMC_Modbus.csv";
//                    importIO(csv_path, true);
//                }
//                default -> {
//                    csv_path = viewer.readIOpath();
//                    importIO(csv_path, true);
//                }
//            }
            sfee.setIO_path(csv_path);

            String mode = viewer.opMode(sfee.getName());
            //String mode = "2";
            if (Integer.parseInt(mode) == 1) {
                opMode = operationMode.NORMAL;
            } else {
                opMode = operationMode.PROG_FAILURES;
            }

            ArrayList<SFEI> sfeis = viewer.createSFEIs(sfee);
            sfeis.forEach(sfei -> sfee.getSFEIs().put(sfee.getSFEIs().size(), sfei));

//            if (scene == 8 || scene == 13 || scene == 14) {
//                addNewSFEI_conveyor(
//                        "parts_entry",
//                        "s_emitter",
//                        "s_exit",
//                        Instant.now(),
//                        Instant.now(),
//                        true,
//                        true,
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "exit_conveyor",
//                        true,
//                        false);
//
//                addNewSFEI_conveyor(
//                        "metal_entry",
//                        "s_metal",
//                        "s_metal_remover",
//                        Instant.now(),
//                        Instant.now(),
//                        true,
//                        false,
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "metal_conveyor",
//                        false,
//                        false);
//                addNewSFEI_conveyor(
//                        "green_entry",
//                        "s_green",
//                        "s_green_remover",
//                        Instant.now(),
//                        Instant.now(),
//                        true,
//                        false,
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "green_conveyor",
//                        false,
//                        false);
//                addNewSFEI_conveyor(
//                        "blue_entry",
//                        "s_blue",
//                        "s_blue_remover",
//                        Instant.now(),
//                        Instant.now(),
//                        true,
//                        false,
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "blue_conveyor",
//                        false,
//                        false);
//                addNewSFEI_conveyor(
//                        "parts_exit",
//                        "s_faulty",
//                        "s_faulty_remover",
//                        Instant.now(),
//                        Instant.now(),
//                        true,
//                        false,
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "none",
//                        "faulty_conveyor",
//                        false,
//                        false);
//            }
//
//            switch (scene) {
//                case 9, 10, 11 -> {
//                    int index = scene - 9;
//                    add_CMC_block(index);
//                }
//                case 15, 16, 17 -> {
//                    int index = scene - 15;
//                    add_CMC_block(index);
//                }
//            }
//
//            if (scene == 12) {
//                addNewSFEI_pusher("MCS_Pusher",
//                        "s_pusher_back",
//                        "s_pusher_forward",
//                        Instant.now(),
//                        Instant.now(),
//                        false,
//                        false,
//                        "pusher_back",
//                        "pusher_forward",
//                        true,
//                        false);
//                addNewSFEI_machine("MCS_MC1",
//                        new partDescription(partDescription.material.UNKNOWN, partDescription.form.UNKNOWN),
//                        "s_pusher_forward",
//                        "s_conveyor2",
//                        Instant.now(),
//                        Instant.now(),
//                        false,
//                        false,
//                        "none",
//                        "s_conveyor1",
//                        "machine1_tool",
//                        false,
//                        false);
//                addNewSFEI_machine("MCS_MC2",
//                        new partDescription(partDescription.material.UNKNOWN, partDescription.form.UNKNOWN),
//                        "s_conveyor2",
//                        "sw_endLine",
//                        Instant.now(),
//                        Instant.now(),
//                        false,
//                        false,
//                        "none",
//                        "s_conveyor2",
//                        "machine2_tool",
//                        false,
//                        true);
//            }


//            autoSetSFEE_InOut();
//            autoSetSFEE_function();

            // Initialize SFEE_production_monitor
            sfeeMonitor = new SFEE_production_monitor(sfee/*, mb.readDiscreteInputs()*/);

            String[] visionStr = viewer.associateVisionSensors(sfee);

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
        partDescription partDescription;
        if (index == 0)
            partDescription = new partDescription(models.partDescription.material.METAL, models.partDescription.form.BASE);
        else if (index == 1) {
            partDescription = new partDescription(models.partDescription.material.GREEN, models.partDescription.form.LID);
        } else {
            partDescription = new partDescription(models.partDescription.material.BLUE, models.partDescription.form.LID);
        }
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
                "entry_EP" + index,
                "entry_EB" + index,
                "s_entry_R" + index,
                "s_entry_E" + index,
                "entry_C" + index,
                false,
                false);
        addNewSFEI_machine(
                "MachineCenter_" + index,
                partDescription,
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
                "exit_EP" + index,
                "exit_EB" + index,
                "s_exit_R" + index,
                "s_exit_E" + index,
                "exit_C" + index,
                false,
                false);
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

    public void importIO(String file_path, boolean dbg) {
        sfee.setIo(utils.getInstance().getReader().readModbusTags(file_path, sfee.getName(), dbg));
//        printAllIO();
    }

    public void printAllIO() {
        viewer.listAllIO(sfee.getIo());
    }

    /* ***********************************
                    SFEI
    ************************************ */
    public void addNewSFEI_conveyor(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance,
                                    boolean isSimulation, boolean supportFailures, String aRemover, String aEmit, String aEmitPart, String aEmitBase,
                                    String sRemover, String sEmitter, String aConveyorMotor, boolean is_line_start, boolean is_line_end) {

        sensor_actuator[] vector = new sensor_actuator[7];
        vector[0] = sfee.getIObyName(aRemover);
        vector[1] = sfee.getIObyName(aEmit);
        vector[2] = sfee.getIObyName(aEmitPart);
        vector[3] = sfee.getIObyName(aEmitBase);
        vector[4] = sfee.getIObyName(sRemover);
        vector[5] = sfee.getIObyName(sEmitter);
        vector[6] = sfee.getIObyName(aConveyorMotor);
        SFEI_conveyor newObj = new SFEI_conveyor(
                name,
                sfee.getIObyName(inSensor),
                sfee.getIObyName(outSensor),
                dayOfBirth, dayOfLastMaintenance,
                isSimulation, supportFailures, is_line_start, is_line_end,
                vector);
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

    }

    private void addNewSFEI_pusher(String name, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance,
                                   boolean isSimulation, boolean supportFailures, String aBackMotor, String aForwardMotor,
                                   boolean is_line_start, boolean is_line_end) {

        SFEI_pusher newObj = new SFEI_pusher(
                name,
                sfee.getIObyName(inSensor),
                sfee.getIObyName(outSensor),
                dayOfBirth, dayOfLastMaintenance,
                isSimulation, supportFailures, is_line_start, is_line_end,
                sfee.getIObyName(aBackMotor), sfee.getIObyName(aForwardMotor));

        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

    }

    public void addNewSFEI_machine(String name, partDescription partDescription, String inSensor, String outSensor, Instant dayOfBirth, Instant dayOfLastMaintenance, boolean isSimulation, boolean supportsFailures, String aProduce, String sDoor, String aStop, boolean is_line_start, boolean is_line_end) {
        sensor_actuator[] vector = new sensor_actuator[3];
        vector[0] = sfee.getIObyName(aProduce);
        vector[1] = sfee.getIObyName(sDoor);
        vector[2] = sfee.getIObyName(aStop);
        SFEI_machine newObj = new SFEI_machine(name, partDescription, sfee.getIObyName(inSensor), sfee.getIObyName(outSensor), dayOfBirth, dayOfLastMaintenance, isSimulation, supportsFailures, is_line_start, is_line_end, vector);
        sfee.getSFEIs().put(sfee.getSFEIs().size(), newObj);

    }

    private void autoSetSFEE_InOut() {

        this.sfee.setInSensor(sfee.getSFEIs().get(0).getInSensor());
        this.sfee.setOutSensor(sfee.getSFEIs().get(sfee.getSFEIs().size() - 1).getOutSensor());
    }

    private void autoSetSFEE_function() {

        // Detect if it is the starting of the line, so it is the Sorting Station case!
//        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
//            if (entry.getValue().isLine_start() && sfee.getSFEE_environment().equals(SFEE.SFEE_environment.SIMULATION)) {
//                sfee.setSFEE_function(SFEE.SFEE_role.SORTING_STATION);
//                break;
//            }
//        }
    }


    /* First Run in order to get the minimum working stochasticTime for each element */
//    public void launchSetup() {
//
//        try {
//            List<Callable<Long>> taskList = new ArrayList<>();
//            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
//                taskList.add(new setupRun(sfei.getValue(), mb));
//            }
//            ExecutorService executorService = Executors.newFixedThreadPool(sfee.getSFEIs().size());
//            System.out.print("Press ENTER to start simulation of " + sfee.getName());
//            Scanner in = new Scanner(System.in);
//            in.nextLine();
//
//            launchSimulation();
//            List<Future<Long>> futures = executorService.invokeAll(taskList);
//
//            for (Future<Long> future : futures) {
//                // Waits for all tasks to return
//                future.get();
//            }
//            executorService.shutdown();
//
//            stopSimulation();
//            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
//                System.out.println("SFEI " + sfei.getValue().getName() + " minTime(s): " + sfei.getValue().getMinOperationTime());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void initFailures() {
        if (opMode.equals(operationMode.PROG_FAILURES)) {

            // Before definition, print SFEE minimal operation stochasticTime

            Long[] opTimes = getSFEEOperationTime();
            long totalTime = 0;
            for (Long value : opTimes)
                totalTime = totalTime + value;

            System.out.println("*** WARNING ***");
            System.out.println("    Notice that Element " + sfee.getName() + " has a minimum operation time of " + totalTime + " s");
            System.out.println("    In case of the gaussian value be smaller than the minimum, it will be minor by it! ");


            String[] sfeeTime = viewer.SFEE_stochasticTime(sfee.getName());

            ArrayList<String[]> failures_f = viewer.SFEE_failures(sfee.getName());

            if (sfeeTime[0].contains("gauss")) {
                // Stochastic Time
                sfeeFailures = new SFEE_production_failures(
                        sfee,
                        stochasticTime.timeOptions.GAUSSIAN,
                        new String[]{sfeeTime[1], sfeeTime[2]},
                        failures_f);

            } else if (sfeeTime[0].contains("linear")) {
                // Linear Time
                sfeeFailures = new SFEE_production_failures(
                        sfee,
                        stochasticTime.timeOptions.LINEAR,
                        new String[]{sfeeTime[1], sfeeTime[2]},
                        failures_f);
            }
        }
    }

    public void init_after_XML_load() {

        // Load IO to the SFEE (from the path)
        importIO(sfee.getIO_path(), true);

        if (opMode.equals(operationMode.PROG_FAILURES))
            sfeeFailures.setSfee(sfee);
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

                List<Object> holdRegsValues = new ArrayList<>(mb.readHoldingRegisters());
                holdRegsValues = new ArrayList<>(Collections.nCopies(holdRegsValues.size(), -1));

                // The function mb.readCoils() is only to initialize the list elements with a given size
                ArrayList<List<Object>> inputs = new ArrayList<>();
                inputs.add(discreteInputsState);

                ArrayList<List<Object>> outputs = new ArrayList<>();
                outputs.add(actuatorsState);
                outputs.add(holdRegsValues);

                sfeeFailures.loop(inputs, outputs);
                mb.writeCoils(actuatorsState);
                mb.writeRegisters(holdRegsValues);

                return;
            }

            mb.writeCoils(actuatorsState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchSimulation() {
        if (sfee.getSFEE_environment().equals(SFEE.SFEE_environment.SIMULATION))
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
