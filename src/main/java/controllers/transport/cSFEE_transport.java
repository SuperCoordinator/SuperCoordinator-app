package controllers.transport;

import communication.modbus;
import failures.SFEE_transport_failures;
import failures.stochasticTime;
import models.base.SFE_role;
import models.sfe_x.SFEI_transport;
import models.sfe_x.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.sensor_actuator;
import monitors.transport.SFEE_transport_monitor;
import org.apache.commons.math3.util.Pair;
import utility.serialize.serializer;

import javax.xml.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEE_transport {


    // SFEM_transport based on 1-1 connections between SFEE's
    private SFEE sfee;
    @XmlElement
    private SFEE_transport_monitor sfeeMonitor;
    @XmlElement
    private SFEE_transport_failures sfeeFailures;

    private modbus inMB;
    private modbus outMB;

    private SFEM_transport.configuration configuration;
    @XmlAttribute
    private String prevSFEE_name;
    @XmlAttribute
    private String nextSFEE_name;
    @XmlAttribute
    private String prevSFEI_name;
    @XmlAttribute
    private String nextSFEI_name;

    public cSFEE_transport() {
    }

    public cSFEE_transport(SFEE sfee, SFEM_transport.configuration configuration) {
        this.sfee = sfee;
        this.configuration = configuration;
    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;
    }

    public void setConfiguration(SFEM_transport.configuration configuration) {
        this.configuration = configuration;
    }

    public SFEE getSfee() {
        return sfee;
    }

    /* Only to see if the next SFEE is free or with parts in production*/
    private SFEE nextSFEE;

    public void init(ArrayList<Object> data) {

        SFEE inSFEE = (SFEE) data.get(3);
        SFEI inSFEI = (SFEI) data.get(4);

        SFEE outSFEE = (SFEE) data.get(5);
        SFEI outSFEI = (SFEI) data.get(6);

        this.inMB = (modbus) data.get(1);
        this.outMB = (modbus) data.get(2);

        this.nextSFEE = outSFEE;

        this.prevSFEE_name = inSFEE.getName();
        this.nextSFEE_name = outSFEE.getName();

        this.prevSFEI_name = inSFEI.getName();
        this.nextSFEI_name = outSFEI.getName();

        // I/O Setting
        TreeMap<Integer, sensor_actuator> io = new TreeMap<>();

        // The inSens is the sensor place in the remover (actuator) of last SFEE (SFEI to be more precise)
        io.put(0, inSFEI.getOutSensor());
        io.put(1, inSFEE.getIObyName((String) data.get(7)));

        // ESTA VERIFICAÇÃO TEM DE DEPENDER DO TIPO DE SFEE, SIM OU REAL
        // The outSens is the sensor place on the controller-defined emitter of next SFEE (SFEI to be more precise)
        io.put(2, outSFEI.getInSensor());
        io.put(3, outSFEE.getIObyName((String) data.get(8)));
        io.put(4, outSFEE.getIObyName((String) data.get(9)));
        io.put(5, outSFEE.getIObyName((String) data.get(10)));

        sfee.setIo(io);

        // create new SFEI_transport instance
        SFEI_transport sfeiTransport = new SFEI_transport(
                (String) data.get(0),
                SFEI.SFEI_type.TRANSPORT,
                inSFEI.getOutSensor(),
                outSFEI.getInSensor(),
                Instant.now(),
                Instant.now(),
                io.get(1), io.get(3), io.get(4), io.get(5));


        // ADD SFEI to SFEE
        sfee.getSFEIs().put(0, sfeiTransport);
        autoSetSFEE_InOut();
        // Initialize SFEE_transport_module
        sfeeMonitor = new SFEE_transport_monitor(sfee, inSFEI, outSFEI, configuration);

    }

    public Pair<Pair<String, String>, Pair<String, String>> prevNextSFEE() {
        return new Pair<>(new Pair<>(prevSFEE_name, prevSFEI_name), new Pair<>(nextSFEE_name, nextSFEI_name));
    }

    public void cSFEE_transport_setup(SFEI inSFEI, SFEI outSFEI, modbus inMB, modbus outMB) {
        this.inMB = inMB;
        this.outMB = outMB;
        if (!outSFEI.getSfeiType().equals(SFEI.SFEI_type.WAREHOUSE))
            this.nextSFEE = serializer.getInstance().searchSFEE_SFEIbySFEI_name(outSFEI.getName()).getFirst();
        else {
            this.nextSFEE = serializer.getInstance().getC_Warehouse().getSfem().getSfeeWarehouse();
        }
        sfeeMonitor = new SFEE_transport_monitor(sfee, inSFEI, outSFEI, configuration);
        sfeeFailures.setSfee(sfee);
        sfeeFailures.setConfiguration(configuration);

    }

    private void autoSetSFEE_InOut() {
        this.sfee.setInSensor(sfee.getSFEIs().get(0).getInSensor());
        this.sfee.setOutSensor(sfee.getSFEIs().get(sfee.getSFEIs().size() - 1).getOutSensor());
    }

    public void init_OperationMode(ArrayList<Object> data) {

        String operator = (String) data.get(0);
        String mean = (String) data.get(1);
        String dev = " ";

        if (operator.contains("gauss"))
            dev = (String) data.get(2);

        if (operator.contains("gauss")) {
            // Stochastic Time
            sfeeFailures = new SFEE_transport_failures(
                    sfee,
                    stochasticTime.timeOptions.GAUSSIAN,
                    new String[]{mean, dev}, configuration);

        } else if (operator.contains("linear")) {
            // Linear Time
            sfeeFailures = new SFEE_transport_failures(
                    sfee,
                    stochasticTime.timeOptions.LINEAR,
                    new String[]{mean, dev}, configuration);
        }
    }

    public void loop() {
        // Monitor -> Move parts between inSFEI -> buffer -> outSFEI
        // based on the state of FAILURES, Monitor moves
        try {

            List<Object> discreteInputsState_inMB = new ArrayList<>();
            List<Object> coilsState_inMB = new ArrayList<>();

            List<Object> discreteInputsState_outMB = new ArrayList<>();
            List<Object> holdRegsValues_outMB = new ArrayList<>();
            List<Object> coilsState_outMB = new ArrayList<>();

            switch (configuration) {
                case WH2SFEI, WH2RealSFEI -> {
                    discreteInputsState_outMB = new ArrayList<>(outMB.readDiscreteInputs());
                    coilsState_outMB = new ArrayList<>(outMB.readCoils());
                    coilsState_outMB = new ArrayList<>(Collections.nCopies(coilsState_outMB.size(), -1));
                    holdRegsValues_outMB = new ArrayList<>(outMB.readHoldingRegisters());
                    holdRegsValues_outMB = new ArrayList<>(Collections.nCopies(holdRegsValues_outMB.size(), -1));
                }
                case SFEI2WH, RealSFEI2WH -> {
                    discreteInputsState_inMB = new ArrayList<>(inMB.readDiscreteInputs());
                    coilsState_inMB = new ArrayList<>(inMB.readCoils());
                    coilsState_inMB = new ArrayList<>(Collections.nCopies(coilsState_inMB.size(), -1));
                }
                default -> {
                    discreteInputsState_inMB = new ArrayList<>(inMB.readDiscreteInputs());
                    coilsState_inMB = new ArrayList<>(inMB.readCoils());
                    coilsState_inMB = new ArrayList<>(Collections.nCopies(coilsState_inMB.size(), -1));

                    discreteInputsState_outMB = new ArrayList<>(outMB.readDiscreteInputs());
                    coilsState_outMB = new ArrayList<>(outMB.readCoils());
                    coilsState_outMB = new ArrayList<>(Collections.nCopies(coilsState_outMB.size(), -1));
                    holdRegsValues_outMB = new ArrayList<>(outMB.readHoldingRegisters());
                    holdRegsValues_outMB = new ArrayList<>(Collections.nCopies(holdRegsValues_outMB.size(), -1));
                }
            }

            ArrayList<List<Object>> inputs = new ArrayList<>();
            inputs.add(discreteInputsState_inMB);
            inputs.add(discreteInputsState_outMB);

            ArrayList<List<Object>> outputs = new ArrayList<>();
            outputs.add(coilsState_inMB);
            outputs.add(coilsState_outMB);
            outputs.add(holdRegsValues_outMB);

            sfeeFailures.loop(inputs, outputs, nextSFEE_availability());
            sfeeMonitor.loop(inputs);

            switch (configuration) {
                case WH2SFEI, WH2RealSFEI -> {
                    outMB.writeCoils(coilsState_outMB);
                    outMB.writeRegisters(holdRegsValues_outMB);
                }
                case SFEI2WH, RealSFEI2WH -> inMB.writeCoils(coilsState_inMB);
                default -> {
                    inMB.writeCoils(coilsState_inMB);
                    outMB.writeCoils(coilsState_outMB);
                    outMB.writeRegisters(holdRegsValues_outMB);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean nextSFEE_availability() {
        boolean free = true;
        if (nextSFEE.getSFEE_role().equals(SFE_role.PRODUCTION)) {
            for (SFEI sfei : nextSFEE.getSFEIs().values()) {
                if (sfei.getPartsATM().size() > 0) {
                    free = false;
                    break;
                }
            }
        }
        return free;
    }

}
