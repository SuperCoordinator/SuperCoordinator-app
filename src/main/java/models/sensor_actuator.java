package models;

public record sensor_actuator(String name, models.sensor_actuator.Type type, boolean invLogic,
                              models.sensor_actuator.DataType dataType,
                              models.sensor_actuator.AddressType addressType, int register, int bit_offset) {

    public sensor_actuator changeInvLogic (boolean invLogic){
        return new sensor_actuator(name(),type(),invLogic,dataType(),addressType(),register(),bit_offset());
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


}
