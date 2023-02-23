package utils;

import models.sensor_actuator;

import java.util.TreeMap;

public class search {

    protected TreeMap<String, sensor_actuator> getSensorsOrActuators(TreeMap<String, sensor_actuator> treeMap, boolean is_sensor) {

        TreeMap<String, sensor_actuator> rTreeMap = new TreeMap<>();
        try {
            treeMap.forEach(
                    (obj_key, obj_val) -> {
                        if (is_sensor) {
                            if (obj_val.type().equals(sensor_actuator.Type.INPUT))
                                rTreeMap.put(obj_key, obj_val);
                        } else {
                            if (obj_val.type().equals(sensor_actuator.Type.OUTPUT))
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
}
