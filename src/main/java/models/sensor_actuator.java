package models;

public class sensor_actuator {

    public enum Type{
        INPUT,
        OUTPUT
    }
    public enum DataType{
        BOOL,
        REAL,
        INT
    }

    public enum AddressType{
        /**
         * 1-bit registers, used to control discrete outputs (including bool values), read/write.
         */
        COIL,
        /**
         * 1-bit registers, used as inputs, read only.
         */
        DISCRETE_INPUT,
        /**
         * 16-bit registers, used as inputs, read only.
         */
        INPUT_REGISTER,
        /**
         * 16-bit registers; used for inputs, output, configuration data, or any requirement for “holding” data; read/write.
         */
        HOLDING_REGISTER
    }
    private String name;
    private Type type;
    private DataType dataType;
    private AddressType addressType;
    private int register;
    private int bit_offset;

    public sensor_actuator(String name, Type type, DataType dataType, AddressType addressType, int register, int bit_offset) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.addressType = addressType;
        this.register = register;
        this.bit_offset = bit_offset;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public DataType getDataType() {
        return dataType;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public int getRegister() {
        return register;
    }

    public int getBit_offset() {
        return bit_offset;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public void setBit_offset(int bit_offset) {
        this.bit_offset = bit_offset;
    }
}
