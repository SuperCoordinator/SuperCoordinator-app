package viewers;

import models.sensor_actuator;

import java.util.TreeMap;

public class eduBlock {

    public void listAllIO(TreeMap<String, sensor_actuator> io) {
        io.forEach((key, value) -> System.out.println(
                key + " " + value.getType() + " " + value.getDataType() + " " + value.getAddressType() + " " + value.getBit_offset()));
    }

}
