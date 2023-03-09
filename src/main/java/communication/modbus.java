package communication;

import models.sensor_actuator;
import utils.utils;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;


import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

public class modbus implements Runnable {

    private ModbusTCPMaster con;
    private String ip;
    private int port;
    private int slaveID;

    private TreeMap<Integer, sensor_actuator> io;
    private boolean[] inputs_invLogic;
    private boolean[] outputs_invLogic;
    private AtomicBoolean coilsUpdated = new AtomicBoolean(false);
    private AtomicIntegerArray coils;
    private AtomicIntegerArray discreteInputs;
    private AtomicIntegerArray inputRegisters;
    private AtomicBoolean hRegUpdated = new AtomicBoolean(false);
    private AtomicLongArray holdingRegisters;

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

    public void openConnection(String ip, int port, int slaveID, TreeMap<Integer, sensor_actuator> io) {

        this.ip = ip;
        this.port = port;
        this.slaveID = slaveID;
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
            holdingRegisters = new AtomicLongArray(nIOperAddrType[3]);

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

    private List<Long> runtime = new ArrayList<>();
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
                    discreteInputs.getAndSet(i, inputs_invLogic[i] == bitVector.getBit(i) ? 0 : 1);
                }
            }
/*            if (inputRegisters.length() > 0) {
                InputRegister[] registers = con.readInputRegisters(0, inputRegisters.length());
                for (int i = 0; i < registers.length; i++) {
                    inputRegisters.getAndSet(i, registers[i].getValue());
                }
            }
            if (holdingRegisters.length() > 0) {
                Register[] registers = con.readMultipleRegisters(0, holdingRegisters.length());
                for (int i = 0; i < registers.length; i++) {
                    holdingRegisters.getAndSet(i, registers[i].getValue());
                }
            }*/
            // Write Step
            if (coilsUpdated.get()) {
                BitVector bitVector = new BitVector(coils.length());
                for (int i = 0; i < bitVector.size(); i++) {
                    bitVector.setBit(i, coils.get(i) == 1);
                }
                con.writeMultipleCoils(0, bitVector);
                coilsUpdated.set(false);
                writeLoop++;
            }

            if (hRegUpdated.get()) {
                Register[] registers = new Register[holdingRegisters.length()];
                for (int i = 0; i < registers.length; i++) {
                    registers[i].setValue((int) holdingRegisters.get(i));
                }
                con.writeMultipleRegisters(0, registers);
                hRegUpdated.set(false);
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
                        System.out.println("MB runtime (ms): " + totalRuntime / runtime.size());
                        System.out.println("Runloop: " + runLoop + " Writeloop: " + writeLoop);
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

        for (int i = 0; i < coils.length(); i++) {
            int old = coils.getAndSet(i, (Integer) coilsList.get(i));
            if (old != (Integer) coilsList.get(i))
                coilsUpdated.getAndSet(true);
        }

    }

    public void writeRegisters(List<Object> registers) {

        for (int i = 0; i < holdingRegisters.length(); i++) {
            holdingRegisters.getAndSet(i, (Integer) registers.get(i));
        }
        hRegUpdated.getAndSet(true);
    }

    // For now, only used for Start and Stop F_IO
    public void writeSingleCoil(int offset, int newValue) {
        coils.getAndSet(offset, newValue);
        coilsUpdated.getAndSet(true);
    }


    public List<Object> readState(sensor_actuator input) {

        List<Object> list = new Vector<>();

//        String currentValue = "";
        try {
            switch (input.addressType()) {
                case COIL -> {
                    //System.out.println("COIL");
                    //System.out.println(input.getName() + " reg: " + input.getRegister() + " off: " + input.getBit_offset());
                    BitVector state = con.readCoils(input.register(), input.bit_offset() + 1);
                    boolean boolValue = state.getBit(input.bit_offset());
                    list.add(0, input.invLogic() == boolValue ? 0 : 1);
//                    currentValue = String.valueOf(input.invLogic() != boolValue);
                }
                case DISCRETE_INPUT -> {
                    //System.out.println("DISCRETE_INPUT");
                    //System.out.println(input.getName() + " reg: " + input.getRegister() + " off: " + input.getBit_offset());
                    BitVector state = con.readInputDiscretes(input.register(), input.bit_offset() + 1);
                    boolean boolValue = state.getBit(input.bit_offset());
                    list.add(0, input.invLogic() == boolValue ? 0 : 1);
//                    currentValue = String.valueOf(input.invLogic() != boolValue);

                }
                case INPUT_REGISTER -> {
                    InputRegister[] state = con.readInputRegisters(input.register(), input.bit_offset() + 1);
                    for (InputRegister register : state) {
                        //currentValue = currentValue.concat(register.getValue() + " ");
                        list.add(list.size(), register.getValue());
                    }
                }
                case HOLDING_REGISTER -> {
                    Register[] state = con.readMultipleRegisters(input.register(), input.bit_offset() + 1);
                    for (Register register : state) {
//                        currentValue = currentValue.concat(register.getValue() + " ");
                        list.add(list.size(), register.getValue());
                    }
                }
                default -> {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("readState of " + input.getName() + " : " + currentValue);
//        return currentValue;
        System.out.println(Arrays.toString(list.toArray()));
        return list;
    }

    public List<Object> /*String*/ readMultipleInputs(TreeMap<Integer, sensor_actuator> io) {

//        Instant start_t = Instant.now();


        sensor_actuator input = util.getSearch().getLargestInputOffset(io);
        List<Object> list = new Vector<>();
//        System.out.println("Search time (ms)" + Duration.between(start_t, Instant.now()).toMillis());
//        String currentValue = "";
        try {
//            start_t = Instant.now();
            BitVector state = con.readInputDiscretes(input.register(), input.bit_offset() + 1);
//            System.out.println("MB Response time (ms)" + Duration.between(start_t, Instant.now()).toMillis());
//            start_t = Instant.now();
            ArrayList<sensor_actuator> inputs = new ArrayList<>(util.getSearch().getSensorsOrActuators(io, true).values());
            for (int i = 0; i < state.size(); i++) {
                boolean boolValue = state.getBit(i);
//                currentValue = currentValue.concat((boolValue != inputs.get(i).invLogic()) + " ");
                list.add(list.size(), inputs.get(i).invLogic() == boolValue ? 0 : 1);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //
//        System.out.println("Remaing of Read Inputs (ms) " + Duration.between(start_t, Instant.now()).toMillis());
        return list;
//        return currentValue;
    }

    public String readMultipleCoils(TreeMap<Integer, sensor_actuator> io) {

        sensor_actuator input = util.getSearch().getLargestOutputOffset(io);

        String currentValue = "";
        try {
/*            String[] oneByOne = new String[input.bit_offset() + 1];
            String[] oneByOneName = new String[input.bit_offset() + 1];
            for (Map.Entry<Integer, sensor_actuator> sa : io.entrySet()) {
                if (sa.getValue().type().equals(sensor_actuator.Type.OUTPUT) && sa.getValue().addressType().equals(sensor_actuator.AddressType.COIL)) {
                    oneByOneName[sa.getValue().bit_offset()] = sa.getValue().name();
                    oneByOne[sa.getValue().bit_offset()] = readState(sa.getValue());
                }
            }
            System.out.println(Arrays.toString(oneByOneName));
            System.out.println(Arrays.toString(oneByOne));*/
            BitVector state = con.readCoils(input.register(), input.bit_offset() + 1);
            ArrayList<sensor_actuator> inputs = new ArrayList<>(util.getSearch().getSensorsOrActuators(io, false).values());
            for (int i = 0; i < state.size(); i++) {
                boolean boolValue = state.getBit(i);
                currentValue = currentValue.concat((boolValue != inputs.get(i).invLogic()) + " ");
            }

//            Register[] regs = con.readMultipleRegisters(input.register(), 3);
//            for (int i = 0; i < regs.length; i++) {
//                System.out.println(regs[i].getValue());
///*                boolean boolValue = regs[i].getValue();
//                currentValue = currentValue.concat(boolValue + " ");*/
//            }

//            InputRegister[] inputRegisters = con.readInputRegisters(0,3);
//            for (InputRegister register : inputRegisters){
//                System.out.println(register.getValue());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentValue;
    }


    public void writeState(sensor_actuator input, String newState) {

        try {
            if (input.type() == sensor_actuator.Type.INPUT)
                throw new Exception("It is not possible to write in a sensor address!");

            switch (input.addressType()) {
                case COIL -> {
                    //Convert to boolean
                    boolean b_newState = Integer.parseInt(newState) > 0;
                    con.writeCoil(slaveID, input.bit_offset(), b_newState);
                }
                case HOLDING_REGISTER -> {
                    Register[] registers = con.readMultipleRegisters(input.register(), input.bit_offset() + 1);
                    registers[input.bit_offset()].setValue(Integer.parseInt(newState));

                    con.writeMultipleRegisters(input.register(), registers);
                }
                default -> {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeMultipleCoils(TreeMap<Integer, sensor_actuator> io, String newStates) {

        try {
            String[] values = newStates.split(" ");
            BitVector bitVector = new BitVector(values.length);

            ArrayList<sensor_actuator> outputs = new ArrayList<>(util.getSearch().getSensorsOrActuators(io, false).values());

            for (int i = 0; i < values.length; i++) {
                bitVector.setBit(i, Boolean.parseBoolean(values[i]) != outputs.get(i).invLogic());
            }
//            System.out.println("Bit Vector:" + bitVector);
            con.writeMultipleCoils(0, bitVector);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
