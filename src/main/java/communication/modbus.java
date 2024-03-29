package communication;

import models.sensor_actuator;
import net.wimpi.modbus.procimg.SimpleRegister;
import utility.utils;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;


import javax.xml.bind.annotation.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class modbus implements Runnable {
    private ModbusTCPMaster con;
    @XmlAttribute
    private String ip;
    @XmlAttribute
    private int port;
    @XmlAttribute
    private int slaveID;

    public modbus() {
    }

    public modbus(String ip, int port, int slaveID) {
        this.ip = ip;
        this.port = port;
        this.slaveID = slaveID;
    }

    private TreeMap<Integer, sensor_actuator> io;
    private boolean[] inputs_invLogic;
    private boolean[] outputs_invLogic;
    private final AtomicBoolean coilsUpdated = new AtomicBoolean(false);
    private AtomicIntegerArray coils;
    private AtomicIntegerArray discreteInputs;
    private AtomicIntegerArray inputRegisters;
    private final AtomicBoolean hRegUpdated = new AtomicBoolean(false);
    private AtomicIntegerArray holdingRegisters;

    private boolean configured = false;

    private void connect() {
        try {
            con = new ModbusTCPMaster(ip, port);
            con.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openConnection(TreeMap<Integer, sensor_actuator> io) {

        this.io = new TreeMap<>(io);
        this.configured = true;
        connect();

        setAtomicArrays();
        detectInverseLogicIO();
    }

    public void reOpenConnection() {
        connect();
    }


    public String getIp() {
        return ip;
    }


    public int getPort() {
        return port;
    }


    public int getSlaveID() {
        return slaveID;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void closeConnection() {
        con.disconnect();
        con = null;
    }

    private void setAtomicArrays() {

        int[] nIOperAddrType = utils.getInstance().getSearch().getLargestOffsetperAddressType(io);

        if (nIOperAddrType[0] > 0)
            coils = new AtomicIntegerArray(nIOperAddrType[0]);
        if (nIOperAddrType[1] > 0)
            discreteInputs = new AtomicIntegerArray(nIOperAddrType[1]);
        if (nIOperAddrType[2] > 0)
            inputRegisters = new AtomicIntegerArray(nIOperAddrType[2]);
        if (nIOperAddrType[3] > 0)
            holdingRegisters = new AtomicIntegerArray(nIOperAddrType[3]);

    }

    private void detectInverseLogicIO() {
        ArrayList<sensor_actuator> inputs = new ArrayList<>(utils.getInstance().getSearch().getSensorsOrActuators(io, true).values());
        inputs.removeIf(sensorActuator -> !sensorActuator.getAddressType().equals(sensor_actuator.AddressType.DISCRETE_INPUT));

        if (inputs.size() > 0) {
            inputs_invLogic = new boolean[inputs.size()];
            for (int i = 0; i < inputs.size(); i++) {
                inputs_invLogic[i] = inputs.get(i).getInvLogic();
            }
        }
        ArrayList<sensor_actuator> outputs = new ArrayList<>(utils.getInstance().getSearch().getSensorsOrActuators(io, false).values());
        outputs.removeIf(sensorActuator -> !sensorActuator.getAddressType().equals(sensor_actuator.AddressType.COIL));

        if (outputs.size() > 0) {
            outputs_invLogic = new boolean[outputs.size()];
            for (int i = 0; i < outputs.size(); i++) {
                outputs_invLogic[i] = outputs.get(i).getInvLogic();
            }
        }

    }

    @Override
    public void run() {
        try {
            if (con != null) {

                if (discreteInputs != null) {
                    if (discreteInputs.length() > 0) {
                        BitVector bitVector = con.readInputDiscretes(0, discreteInputs.length());
                        for (int i = 0; i < bitVector.size(); i++) {
                            // Evaluation of the discrete inputs (sensors) logic
                            discreteInputs.getAndSet(i, inputs_invLogic[i] == bitVector.getBit(i) ? 0 : 1);
                        }
                    }
                }
                if (inputRegisters != null) {
                    if (inputRegisters.length() > 0) {
                        InputRegister[] registers = con.readInputRegisters(0, inputRegisters.length());
                        for (int i = 0; i < registers.length; i++) {
                            inputRegisters.getAndSet(i, registers[i].getValue());
                        }
                    }
                }

                // Write Step
                if (coils != null) {
                    if (coilsUpdated.get()) {
                        BitVector bitVector = new BitVector(coils.length());
                        for (int i = 0; i < bitVector.size(); i++) {
                            // Evaluation of the coils (outputs) logic
                            bitVector.setBit(i, outputs_invLogic[i] ? coils.get(i) == 0 : coils.get(i) == 1);
                        }
                        con.writeMultipleCoils(0, bitVector);
                        coilsUpdated.set(false);
                    }
                }
                if (holdingRegisters != null) {
                    if (hRegUpdated.get()) {
                        Register[] registers = new Register[holdingRegisters.length()];
                        //                Register reg  = new SimpleRegister()
                        for (int i = 0; i < registers.length; i++) {
                            registers[i] = new SimpleRegister(holdingRegisters.get(i));
                        }

                        con.writeMultipleRegisters(0, registers);
                        hRegUpdated.set(false);
                    }
                }

            }

        } catch (Exception e) {
            // In child thread, it must print the Exception because the main thread do not catch Runtime Exception from the others
            e.printStackTrace();
        }

    }

    public List<Object> readCoils() {
        if (coils == null)
            return new ArrayList<>();
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < coils.length(); i++) {
            list.add(i, coils.get(i));
        }
        return list;
    }

    public List<Object> readDiscreteInputs() {
        if (discreteInputs == null)
            return new ArrayList<>();
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < discreteInputs.length(); i++) {
            list.add(i, discreteInputs.get(i));
        }
        return list;
    }

    public List<Object> readInputRegisters() {
        if (inputRegisters == null)
            return new ArrayList<>();
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < inputRegisters.length(); i++) {
            list.add(i, inputRegisters.get(i));
        }
        return list;
    }

    public List<Object> readHoldingRegisters() {
        if (holdingRegisters == null)
            return new ArrayList<>();
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < holdingRegisters.length(); i++) {
            list.add(i, holdingRegisters.get(i));
        }
        return list;
    }

    public void writeCoils(List<Object> coilsList) {
        try {
            if (coils != null) {
                for (int i = 0; i < coils.length(); i++) {
                    if ((Integer) coilsList.get(i) == -1) {
                        continue;
                    }
                    if ((int) coilsList.get(i) != coils.get(i)) {
                        coils.getAndSet(i, (Integer) coilsList.get(i));
                        coilsUpdated.getAndSet(true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeRegisters(List<Object> registers) {
        try {
            if (holdingRegisters != null) {

                for (int i = 0; i < holdingRegisters.length(); i++) {
                    if ((Integer) registers.get(i) == -1) {
                        continue;
                    }
                    if ((int) registers.get(i) != holdingRegisters.get(i)) {
                        holdingRegisters.getAndSet(i, (Integer) registers.get(i));
                        hRegUpdated.getAndSet(true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // For now, only used for Start and Stop F_IO
    public void writeSingleCoil(int offset, int newValue) {
        if (coils != null) {
            coils.getAndSet(offset, newValue);
            coilsUpdated.getAndSet(true);
        }
    }

}
