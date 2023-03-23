package communication;

import models.base.SFEE;
import models.base.SFEI;
import models.sensor_actuator;
import net.wimpi.modbus.procimg.SimpleRegister;
import utils.utils;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

public class modbus implements Runnable, Externalizable {
    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(ip);
        out.writeInt(port);
        out.writeInt(slaveID);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.ip = (String) in.readObject();
        this.port = in.readInt();
        this.slaveID = in.readInt();
    }

    private ModbusTCPMaster con;
    private String ip;
    private int port;
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

    private final utils util = new utils();
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
    }

    private void setAtomicArrays() {

        int[] nIOperAddrType = util.getSearch().getLargestOffsetperAddressType(io);

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
        ArrayList<sensor_actuator> inputs = new ArrayList<>(util.getSearch().getSensorsOrActuators(io, true).values());
        inputs.removeIf(sensorActuator -> !sensorActuator.addressType().equals(sensor_actuator.AddressType.DISCRETE_INPUT));

        if (inputs.size() > 0) {
            inputs_invLogic = new boolean[inputs.size()];
            for (int i = 0; i < inputs.size(); i++) {
                inputs_invLogic[i] = inputs.get(i).invLogic();
            }
        }
        ArrayList<sensor_actuator> outputs = new ArrayList<>(util.getSearch().getSensorsOrActuators(io, false).values());
        outputs.removeIf(sensorActuator -> !sensorActuator.addressType().equals(sensor_actuator.AddressType.COIL));

        if (outputs.size() > 0) {
            outputs_invLogic = new boolean[outputs.size()];
            for (int i = 0; i < outputs.size(); i++) {
                outputs_invLogic[i] = outputs.get(i).invLogic();
            }
        }

    }

    private final List<Long> runtime = new ArrayList<>();
    private final Instant init_t = Instant.now();
    private boolean printDBG = false;

    private long runLoop = 0;
    private long writeLoop = 0;

    @Override
    public void run() {
        try {
            Instant start_t = Instant.now();

            // Read Step
/*            if (coils.length() > 0) {
                BitVector bitVector = con.readCoils(0, coils.length());
                for (int i = 0; i < bitVector.size(); i++) {
                    coils.getAndSet(i, outputs_invLogic[i] == bitVector.getBit(i) ? 0 : 1);
                }
            }*/

            if (discreteInputs.length() > 0) {
                BitVector bitVector = con.readInputDiscretes(0, discreteInputs.length());
                for (int i = 0; i < bitVector.size(); i++) {
                    // Evaluation of the discrete inputs (sensors) logic
                    discreteInputs.getAndSet(i, inputs_invLogic[i] == bitVector.getBit(i) ? 0 : 1);
                }
            }
            if (inputRegisters.length() > 0) {
                InputRegister[] registers = con.readInputRegisters(0, inputRegisters.length());
                for (int i = 0; i < registers.length; i++) {
                    inputRegisters.getAndSet(i, registers[i].getValue());
                }
            }
/*              if (holdingRegisters.length() > 0) {
                Register[] registers = con.readMultipleRegisters(0, holdingRegisters.length());
                for (int i = 0; i < registers.length; i++) {
                    holdingRegisters.getAndSet(i, registers[i].getMean());
                }
            }*/

            // Write Step
            if (coilsUpdated.get()) {
                BitVector bitVector = new BitVector(coils.length());
                for (int i = 0; i < bitVector.size(); i++) {
                    // Evaluation of the coils (outputs) logic
                    bitVector.setBit(i, outputs_invLogic[i] ? coils.get(i) == 0 : coils.get(i) == 1);
                }
                con.writeMultipleCoils(0, bitVector);
                coilsUpdated.set(false);
                writeLoop++;
            }

            if (hRegUpdated.get()) {
                Register[] registers = new Register[holdingRegisters.length()];
//                Register reg  = new SimpleRegister()
                for (int i = 0; i < registers.length; i++) {
                    registers[i] = new SimpleRegister(holdingRegisters.get(i));
                }
                con.writeMultipleRegisters(0, registers);
                hRegUpdated.set(false);
                writeLoop++;
            }
            runtime.add(Duration.between(start_t, Instant.now()).toMillis());
//            System.out.println("MB execution time (ms) " + Duration.between(start_t, Instant.now()).toMillis());
            if (Duration.between(init_t, Instant.now()).toSeconds() > 30) {
                if (Duration.between(init_t, Instant.now()).toSeconds() % 5 == 0) {
                    if (!printDBG) {

                        long totalRuntime = 0;
                        for (Long run_t : runtime) {
                            totalRuntime = totalRuntime + run_t;
                        }
//                        System.out.println("MB runtime (ms): " + totalRuntime / runtime.size());
//                        System.out.println("Runloop: " + runLoop + " Writeloop: " + writeLoop);
                        printDBG = true;
                    }
                } else {
                    printDBG = false;
                }
            }

            runLoop++;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<Object> readCoils() {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < coils.length(); i++) {
            list.add(i, coils.get(i));
        }
        return list;
    }

    public List<Object> readDiscreteInputs() {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < discreteInputs.length(); i++) {
            list.add(i, discreteInputs.get(i));
        }
        return list;
    }

    public List<Object> readInputRegisters() {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < inputRegisters.length(); i++) {
            list.add(i, inputRegisters.get(i));
        }
        return list;
    }

    public List<Object> readHoldingRegisters() {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < holdingRegisters.length(); i++) {
            list.add(i, holdingRegisters.get(i));
        }
        return list;
    }

    public void writeCoils(List<Object> coilsList) {
        try {
            for (int i = 0; i < coils.length(); i++) {
                if ((Integer) coilsList.get(i) == -1) {
                    continue;
                }
                int old = coils.getAndSet(i, (Integer) coilsList.get(i));
                if (old != (Integer) coilsList.get(i))
                    coilsUpdated.getAndSet(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeRegisters(List<Object> registers) {
        try {
            for (int i = 0; i < holdingRegisters.length(); i++) {
                if ((Integer) registers.get(i) == -1) {
                    continue;
                }
                int old = holdingRegisters.getAndSet(i, (Integer) registers.get(i));
                if (old != (Integer) registers.get(i)) {
                    hRegUpdated.getAndSet(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // For now, only used for Start and Stop F_IO
    public void writeSingleCoil(int offset, int newValue) {
        coils.getAndSet(offset, newValue);
        coilsUpdated.getAndSet(true);
    }

}
