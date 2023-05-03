package communication.database.interfaces;

import java.util.List;

public interface I_sf_configuration {
    int insert(String sf_config_name);
    void delete(String sf_config_name);
    void update(String old_sf_config_name,String new_sf_config_name);
    List<String> getAll_sf_configurations();

}
