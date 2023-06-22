package communication.database.mediators;

import java.util.List;

public interface IM_sensor {

    void insert(String name, String fk_sfei, boolean inSensor);

    void delete(String name, String fk_sfei);

    void update(String old_name, String new_name, String fk_sfei);

    List<String> getAll_sensors();

    List<String> getAll_sensorsFrom(String fk_sfei);

}
