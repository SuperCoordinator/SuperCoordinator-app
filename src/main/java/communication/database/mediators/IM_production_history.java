package communication.database.mediators;

import java.util.List;

public interface IM_production_history {

    void insert(int fk_part_id, String fk_sensor_name,String material, String form);

    void delete(int part_id);

//    void update(String old_sf_config_name, String new_sf_config_name);

    List<String> getProduction_History();
    List<String> getProduction_History_of(int part_id);

}
