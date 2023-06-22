package models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public final class sensor_actuator  {

    public enum Type {
        INPUT,
        OUTPUT
    }

    public enum DataType {
        BOOL,
        REAL,
        INT
    }

    public enum AddressType {
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
    @XmlAttribute
    private String name;
    @XmlAttribute
    private Type type;
    @XmlAttribute
    private boolean invLogic;
    @XmlAttribute
    private DataType dataType;
    @XmlAttribute
    private AddressType addressType;
    @XmlAttribute
    private int register;
    @XmlAttribute
    private int offset;

    public sensor_actuator() {
    }

    public sensor_actuator(String name, Type type, boolean invLogic,
                           DataType dataType,
                           AddressType addressType, int register, int offset) {
        this.name = name;
        this.type = type;
        this.invLogic = invLogic;
        this.dataType = dataType;
        this.addressType = addressType;
        this.register = register;
        this.offset = offset;
    }

    public sensor_actuator changeInvLogic(boolean invLogic) {
        return new sensor_actuator(getName(), getType(), invLogic, getDataType(), getAddressType(), getRegister(), getOffset());
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

    public boolean getInvLogic() {
        return invLogic;
    }

    public int getRegister() {
        return register;
    }

    public int getOffset() {
        return offset;
    }

}
