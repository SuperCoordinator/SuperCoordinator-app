package communication.database;

public interface IM_dbConnection {

    void registerM_sf_configuration();

    void registerM_inbound_order();

    void registerM_outbound_order();

    void registerM_part();

    void registerM_production_history();

    void registerM_sfem();

    void registerM_sfee();

    void registerM_sfei();

    void registerM_sensor();

}
