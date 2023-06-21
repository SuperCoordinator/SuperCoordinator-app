package utility;

import models.sensor_actuator;

import java.util.*;

public class search {

    public TreeMap<Integer, sensor_actuator> getSensorsOrActuators(TreeMap<Integer, sensor_actuator> treeMap, boolean is_sensor) {

        TreeMap<Integer, sensor_actuator> rTreeMap = new TreeMap<>();
        try {
            treeMap.forEach(
                    (obj_key, obj_val) -> {
                        if (is_sensor) {
                            if (obj_val.getType().equals(sensor_actuator.Type.INPUT))
                                rTreeMap.put(obj_key, obj_val);
                        } else {
                            if (obj_val.getType().equals(sensor_actuator.Type.OUTPUT))
                                rTreeMap.put(obj_key, obj_val);
                        }
                    });
            if (rTreeMap.size() == 0)
                throw new Exception("rTreeMap is empty!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rTreeMap;
    }

    public int[] getLargestOffsetperAddressType(TreeMap<Integer, sensor_actuator> treeMap) {

        // coils, discrete inputs, input registers, holding registers
        int[] cnts = new int[]{0, 0, 0, 0};

        for (Map.Entry<Integer, sensor_actuator> entry : treeMap.entrySet()) {
            switch (entry.getValue().getAddressType()) {
                case COIL -> cnts[0]++;
                case DISCRETE_INPUT -> cnts[1]++;
                case INPUT_REGISTER -> cnts[2]++;
                case HOLDING_REGISTER -> cnts[3]++;
            }
        }
        return cnts;
    }

}
