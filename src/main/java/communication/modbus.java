package communication;

import models.sensor_actuator;
import utils.utils;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class modbus {

    private ModbusTCPMaster con;
    private String ip;
    private int port;
    private int slaveID;

    private boolean configured = false;

    public void openConnection(String ip, int port, int slaveID) {

        this.ip = ip;
        this.port = port;
        this.slaveID = slaveID;

        this.configured = true;

        try {
            con = new ModbusTCPMaster(ip, port);
            con.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void reOpenConnection() {
        try {
            con = new ModbusTCPMaster(ip, port);
            con.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public String readState(sensor_actuator input) {

        String currentValue = "";
        try {
            switch (input.addressType()) {
                case COIL -> {
                    //System.out.println("COIL");
                    //System.out.println(input.getName() + " reg: " + input.getRegister() + " off: " + input.getBit_offset());
                    BitVector state = con.readCoils(input.register(), input.bit_offset() + 1);
                    boolean boolValue = state.getBit(input.bit_offset());
                    currentValue = String.valueOf(input.invLogic() != boolValue);
                }
                case DISCRETE_INPUT -> {
                    //System.out.println("DISCRETE_INPUT");
                    //System.out.println(input.getName() + " reg: " + input.getRegister() + " off: " + input.getBit_offset());
                    BitVector state = con.readInputDiscretes(input.register(), input.bit_offset() + 1);
                    boolean boolValue = state.getBit(input.bit_offset());
                    currentValue = String.valueOf(input.invLogic() != boolValue);

                }
                case INPUT_REGISTER -> {
                    InputRegister[] state = con.readInputRegisters(input.register(), input.bit_offset() + 1);
                    for (InputRegister register : state) {
                        currentValue = currentValue.concat(register.getValue() + " ");
                    }
                }
                case HOLDING_REGISTER -> {
                    Register[] state = con.readMultipleRegisters(input.register(), input.bit_offset() + 1);
                    for (Register register : state) {
                        currentValue = currentValue.concat(register.getValue() + " ");
                    }
                }
                default -> {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("readState of " + input.getName() + " : " + currentValue);
        return currentValue;
    }

    public String readMultipleInputs(TreeMap<Integer, sensor_actuator> io) {

        utils util = new utils();
        sensor_actuator input = util.getSearch().getLargestInputOffset(io);

        String currentValue = "";
        try {
            BitVector state = con.readInputDiscretes(input.register(), input.bit_offset() + 1);
            ArrayList<sensor_actuator> inputs = new ArrayList<>(util.getSearch().getSensorsOrActuators(io, true).values());
            for (int i = 0; i < state.size(); i++) {
                boolean boolValue = state.getBit(i);
                currentValue = currentValue.concat((boolValue != inputs.get(i).invLogic()) + " ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //

        return currentValue;
    }

    public String readMultipleCoils(TreeMap<Integer, sensor_actuator> io) {
        utils util = new utils();
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
        utils util = new utils();
        try {
            String[] values = newStates.split(" ");
            BitVector bitVector = new BitVector(values.length);

            ArrayList<sensor_actuator> inputs = new ArrayList<>(util.getSearch().getSensorsOrActuators(io, false).values());

            for (int i = 0; i < values.length; i++) {
                bitVector.setBit(i, Boolean.parseBoolean(values[i]) != inputs.get(i).invLogic());
            }
//            System.out.println("Bit Vector:" + bitVector);
            con.writeMultipleCoils(0, bitVector);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
