package communication.database.mediators;

import java.util.List;

public interface IM_sf_configuration {
    void insert(String sf_config_name);
    void delete(String sf_config_name);
    void update(String old_sf_config_name,String new_sf_config_name);
    List<String> getAll_sf_configurations();

}
