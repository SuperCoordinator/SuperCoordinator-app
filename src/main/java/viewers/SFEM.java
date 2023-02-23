package viewers;

import models.sensor_actuator;

import java.util.Arrays;
import java.util.TreeMap;

public class SFEM {

    public void listAllIO(TreeMap<String, sensor_actuator> io) {
        io.forEach((key, value) -> System.out.println(value.toString()));
    }

}
