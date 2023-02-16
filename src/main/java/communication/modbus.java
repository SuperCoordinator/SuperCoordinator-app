package communication;

import models.sensor_actuator;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;

import java.util.TreeMap;

public class modbus {

    private ModbusTCPMaster con;
    private String ip;
    private int port;
    private int slaveID;

    private TreeMap<String, sensor_actuator> io;

    public void openConnection(String ip, int port, int slaveID, TreeMap<String, sensor_actuator> io) {

        this.ip = ip;
        this.port = port;
        this.slaveID = slaveID;
        this.io = io;

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

    public void closeConnection() {
        con.disconnect();
    }

    public String readState(String io_name) {
        sensor_actuator input = io.getOrDefault(io_name, null);
        String currentValue = "";
        try {
            switch (input.getAddressType()) {
                case COIL -> {
                    //System.out.println("COIL");
                    //System.out.println(input.getName() + " reg: " + input.getRegister() + " off: " + input.getBit_offset());
                    BitVector state = con.readCoils(input.getRegister(), input.getBit_offset() + 1);
                    currentValue = String.valueOf(state.getBit(input.getBit_offset()));
                }
                case DISCRETE_INPUT -> {
                    //System.out.println("DISCRETE_INPUT");
                    //System.out.println(input.getName() + " reg: " + input.getRegister() + " off: " + input.getBit_offset());
                    BitVector state = con.readInputDiscretes(input.getRegister(), input.getBit_offset() + 1);
                    currentValue = String.valueOf(state.getBit(input.getBit_offset()));

                }
                case INPUT_REGISTER -> {
                    InputRegister[] state = con.readInputRegisters(input.getRegister(), input.getBit_offset() + 1);
                    for (InputRegister register : state) {
                        currentValue = currentValue.concat(register.getValue() + " ");
                    }

                }
                case HOLDING_REGISTER -> {
                    Register[] state = con.readMultipleRegisters(input.getRegister(), input.getBit_offset() + 1);
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

    public void writeState(String io_name, String newState) {
        sensor_actuator input = io.getOrDefault(io_name, null);
        try {
            if (input.getType() == sensor_actuator.Type.INPUT)
                throw new Exception("It is not possible to write in a sensor address!");

            switch (input.getAddressType()) {
                case COIL -> {
                    //Convert to boolean
                    boolean b_newState = Integer.parseInt(newState) > 0;
                    con.writeCoil(slaveID, input.getBit_offset(), b_newState);
                }
                case HOLDING_REGISTER -> {
                    Register[] registers = con.readMultipleRegisters(input.getRegister(), input.getBit_offset() + 1);
                    registers[input.getBit_offset()].setValue(Integer.parseInt(newState));

                    con.writeMultipleRegisters(input.getRegister(), registers);
                }
                default -> {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
