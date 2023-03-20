package controllers;

import communication.modbus;
import failures.SFEE_transport_failures;
import failures.newVersion.SFEE_failures2;
import failures.stochasticTime;
import models.SFEx_particular.SFEI_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.sensor_actuator;
import monitor.SFEE_monitor;
import monitor.SFEE_transport_monitor;
import org.apache.commons.math3.util.Pair;
import viewers.SFEE_transport;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class cSFEE_transport {

    // SFEM_transport based on 1-1 connections between SFEE's
    private final SFEE sfee;
    private SFEE_transport_monitor sfeeMonitor;

    private SFEE_transport_failures sfeeFailures;

    private final modbus inMB;
    private final modbus outMB;

    private SFEE_transport viewer;

    public cSFEE_transport(SFEE sfee, modbus inMB, modbus outMB) {
        this.sfee = sfee;
        this.inMB = inMB;
        this.outMB = outMB;

        this.viewer = new SFEE_transport();
    }

    public void cSFEE_transport_init(SFEE inSFEE, SFEE outSFEE) {

        // Save SFEI's -> access parts list
        SFEI inSFEI = inSFEE.getSFEIbyIndex(inSFEE.getSFEIs().size() - 1);
        // Set the transportation flag in the last SFEI of the in SFEE
        // In that way, the SFEM monitor will not remove the part from the
        // SFEI list of parts
        inSFEI.setForTransport(true);

        // I/O Setting
        TreeMap<Integer, sensor_actuator> io = new TreeMap<>();

        // The inSens is the sensor place in the remover (actuator) of last SFEE (SFEI to be more precise)
        String[] in_SensAct = viewer.associateSensor2Actuator(1, inSFEE.getOutSensor().name());
        io.put(0,inSFEE.getOutSensor());
        io.put(1,inSFEE.getIObyName(in_SensAct[0]));

        // The outSens is the sensor place on the controller-defined emitter of next SFEE (SFEI to be more precise)
        String[] out_SensAct = viewer.associateSensor2Actuator(3, outSFEE.getInSensor().name());
        io.put(2,outSFEE.getInSensor());
        io.put(3,outSFEE.getIObyName(out_SensAct[0]));
        io.put(4,outSFEE.getIObyName(out_SensAct[1]));
        io.put(5,outSFEE.getIObyName(out_SensAct[2]));

        sfee.setIo(io);

        // create new SFEI_transport instance
        SFEI_transport sfeiTransport = new SFEI_transport(
                "sfei_transport",
                SFEI.SFEI_type.TRANSPORT,
                inSFEE.getOutSensor(),
                outSFEE.getInSensor(),
                Instant.now(),
                Instant.now(),
                sfee.getIObyName(in_SensAct[0]),
                sfee.getIObyName(out_SensAct[0]),
                sfee.getIObyName(out_SensAct[1]),
                sfee.getIObyName(out_SensAct[2]));


        // ADD SFEI to SFEE
        sfee.getSFEIs().put(0, sfeiTransport);

        // Initialize SFEE_transport_module
        sfeeMonitor = new SFEE_transport_monitor(sfee);

    }

    public void initOperationMode() {

        String[] sfeeTime = viewer.SFEE_stochasticTime();


        if (sfeeTime[0].contains("gauss")) {
            // Stochastic Time
            sfeeFailures = new SFEE_transport_failures(
                    sfee,
                    stochasticTime.timeOptions.GAUSSIAN,
                    new String[]{sfeeTime[1], sfeeTime[2]});

        } else if (sfeeTime[0].contains("linear")) {
            // Linear Time
            sfeeFailures = new SFEE_transport_failures(
                    sfee,
                    stochasticTime.timeOptions.LINEAR,
                    new String[]{sfeeTime[1], sfeeTime[2]});
        }
    }

    public void loop() {
        // Monitor -> Move parts between inSFEI -> buffer -> outSFEI
        // based on the state of FAILURES, Monitor moves

        List<Object> discreteInputsState_inMB = new ArrayList<>(inMB.readDiscreteInputs());
//        List<Object> inputRegsValue = new ArrayList<>(inMB.readInputRegisters());

//        System.out.println(Arrays.toString(inputRegsValue.toArray()));
        List<Object> coilsState_inMB = new ArrayList<>(inMB.readCoils());
//        List<Object> holdRegsValues = new ArrayList<>(inMB.readHoldingRegisters());

        List<Object> discreteInputsState_outMB = new ArrayList<>(outMB.readDiscreteInputs());
//        List<Object> inputRegsValue = new ArrayList<>(inMB.readInputRegisters());

//        System.out.println(Arrays.toString(inputRegsValue.toArray()));
        List<Object> coilsState_outMB = new ArrayList<>(outMB.readCoils());
        List<Object> holdRegsValues_outMB = new ArrayList<>(outMB.readHoldingRegisters());


        ArrayList<List<Object>> inputs = new ArrayList<>();
        inputs.add(discreteInputsState_inMB);
        inputs.add(discreteInputsState_outMB);

        ArrayList<List<Object>> outputs = new ArrayList<>();
        outputs.add(coilsState_inMB);
        outputs.add(coilsState_outMB);
        outputs.add(holdRegsValues_outMB);

        sfeeFailures.loop(inputs, outputs);


        inMB.writeCoils(coilsState_inMB);

        outMB.writeCoils(coilsState_outMB);
        outMB.writeRegisters(holdRegsValues_outMB);

    }


}
