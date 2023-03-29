package models;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@XmlRootElement(name = "sensor_actuator")
public final class sensor_actuator implements Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeObject(type);
        out.writeBoolean(invLogic);
        out.writeObject(dataType);
        out.writeObject(addressType);
        out.writeInt(register);
        out.writeInt(bit_offset);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = (String) in.readObject();
        this.type = (Type) in.readObject();
        this.invLogic = in.readBoolean();
        this.dataType = (DataType) in.readObject();
        this.addressType = (AddressType) in.readObject();
        this.register = in.readInt();
        this.bit_offset = in.readInt();
    }

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

    private String name;
    private Type type;
    private boolean invLogic;
    private DataType dataType;
    private AddressType addressType;
    private int register;
    private int bit_offset;

    public sensor_actuator() {
    }

    public sensor_actuator(String name, Type type, boolean invLogic,
                           DataType dataType,
                           AddressType addressType, int register, int bit_offset) {
        this.name = name;
        this.type = type;
        this.invLogic = invLogic;
        this.dataType = dataType;
        this.addressType = addressType;
        this.register = register;
        this.bit_offset = bit_offset;
    }

    public sensor_actuator changeInvLogic(boolean invLogic) {
        return new sensor_actuator(getName(), getType(), invLogic, getDataType(), getAddressType(), getRegister(), getBit_offset());
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    @XmlAttribute(name = "type")
    public Type getType() {
        return type;
    }

    @XmlAttribute(name = "data_type")
    public DataType getDataType() {
        return dataType;
    }

    @XmlAttribute(name = "address_type")
    public AddressType getAddressType() {
        return addressType;
    }

    @XmlAttribute(name = "inverse_logic")
    public boolean getInvLogic() {
        return invLogic;
    }

    @XmlAttribute(name = "register")
    public int getRegister() {
        return register;
    }

    @XmlAttribute(name = "bit_offset")
    public int getBit_offset() {
        return bit_offset;
    }

}
