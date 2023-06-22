package controllers.production;

import communication.modbus;
//import failures.oldVersion.SFEE_failures;
import failures.SFEE_production_failures;
import failures.stochasticTime;
import models.base.SFEE;
import models.base.SFEI;
import models.sensor_actuator;
import monitors.production.SFEE_production_monitor;
import utility.utils;

import javax.xml.bind.annotation.*;
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

    private final viewers.SFEE viewer = new viewers.SFEE();

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

    public void init() {
        try {

            String csv_path = viewer.getIOpath(sfee.getName());

            importIO(csv_path, true);

            String mode = viewer.opMode(sfee.getName());
            if (Integer.parseInt(mode) == 1) {
                opMode = operationMode.NORMAL;
            } else {
                opMode = operationMode.PROG_FAILURES;
            }

            ArrayList<SFEI> sfeis = viewer.createSFEIs(sfee, opMode.equals(operationMode.PROG_FAILURES));
            sfeis.forEach(sfei -> sfee.getSFEIs().put(sfee.getSFEIs().size(), sfei));

            int[] start_endLine_sfeis_idx = viewer.startEnd_sfeis(sfee);

            if (start_endLine_sfeis_idx[0] < sfee.getSFEIs().size())
                sfee.getSFEIs().get(start_endLine_sfeis_idx[0]).setLine_start(true);

            if (start_endLine_sfeis_idx[1] < sfee.getSFEIs().size())
                sfee.getSFEIs().get(start_endLine_sfeis_idx[1]).setLine_end(true);

            sfeeMonitor = new SFEE_production_monitor(sfee/*, mb.readDiscreteInputs()*/);

            String[] visionStr = viewer.associateVisionSensors(sfee);

            if (!visionStr[0].equals("no")) {
                TreeMap<Integer, sensor_actuator> treeMap = new TreeMap<>();
                treeMap.put(Integer.parseInt(visionStr[2]), sfee.getIo().get(Integer.parseInt(visionStr[1])));
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
        sfee.setIO_path(file_path);
        sfee.setIo(utils.getInstance().getReader().readModbusTags(file_path, sfee.getName(), dbg));
    }


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
        if (sfee.getSfeeEnvironment().equals(SFEE.SFEE_environment.SIMULATION))
            mb.writeSingleCoil(sfee.getIObyName("FACTORY I/O (Run)").getOffset(), 1);
        else
            mb.writeSingleCoil(sfee.getIObyName("start_module").getOffset(), 1);
    }

    public void stopSimulation() {

        try {
            if (sfee.getSfeeEnvironment().equals(SFEE.SFEE_environment.SIMULATION)) {
                mb.writeSingleCoil(sfee.getIObyName("FACTORY I/O (Pause)").getOffset(), 1);
                do {
                    Thread.sleep(100);
                }
                while ((int) mb.readDiscreteInputs().get((sfee.getIObyName("FACTORY I/O (Paused)").getOffset())) == 0);
            } else {
                mb.writeSingleCoil(sfee.getIObyName("start_module").getOffset(), 0);
                do {
                    Thread.sleep(100);
                }
                while ((int) mb.readCoils().get((sfee.getIObyName("start_module").getOffset())) == 1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Long[] getSFEEOperationTime() {
        Long[] array = new Long[sfee.getSFEIs().size()];

        for (int i = 0; i < sfee.getSFEIs().size(); i++) {
            array[i] = sfee.getSFEIbyIndex(i).getMinOperationTime();
        }
        return array;
    }


}
