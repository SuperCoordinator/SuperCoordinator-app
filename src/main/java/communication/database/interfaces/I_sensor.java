package communication.database.interfaces;

import java.util.List;

public interface I_sensor {

    int insert(String name, String fk_sfei, boolean inSensor);

    void delete(String name, String fk_sfei);

    void update(String old_name, String new_name, String fk_sfei);

    List<String> getAll_sensors();

    List<String> getAll_sensors_of_SFEI(String fk_sfei);

}
