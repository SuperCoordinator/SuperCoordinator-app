package communication;

import models.sensor_actuator;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;

public class modbus {

    private ModbusTCPMaster con;
    private String ip;
    private int port;
    private int slaveID;

    public void openConnection(String ip, int port, int slaveID) {

        this.ip = ip;
        this.port = port;
        this.slaveID = slaveID;

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


}
