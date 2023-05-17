package communication.database.mediators;

import communication.database.dbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class M_sensor extends queries_buffer implements IM_sensor {

    /**
     * Singleton pattern
     */
    public M_sensor() {
    }

    //
//    public static M_sensor getInstance() {
//        return M_sensor.db_sensorHolder.INSTANCE;
//    }
//
//    private static class db_sensorHolder {
//        private static final M_sensor INSTANCE = new M_sensor();
//    }
    @Override
    public void insert(String name, String fk_sfei, boolean inSensor) {
        try {
//            String def_vars = "SET @name = '" + name + "'," +
//                    " @fk = '" + fk_sfei + "'," +
//                    " @inSensor = " + inSensor + " ;";
            String query = "INSERT INTO sensor (name,fk_sfei,in_sensor)" +
                    "VALUES ('" + name + "','" + fk_sfei + "'," + inSensor + ")" +
                    "ON DUPLICATE KEY UPDATE" +
                    "   name = '" + name + "'," +
                    "   fk_sfei = '" + fk_sfei + "'," +
                    "   in_sensor = " + inSensor + ";";
            getStoredQueries().add(query);
//            Statement st = dbConnection.getInstance().getConnection().createStatement();
//            st.addBatch(def_vars);
//            st.addBatch(query);
//
//            return st.executeBatch()[1];

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String name, String fk_sfei) {

        try {
            String query = "DELETE FROM sensor WHERE name ='" + name + "' AND fk_sfei ='" + fk_sfei + "';";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_name, String new_name, String fk_sfei) {

        try {
            String query = "UPDATE sensor " +
                    "SET name = '" + new_name + "'" +
                    "WHERE name = '" + old_name + "' AND fk_sf_configuration='" + fk_sfei + "';";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public List<String> getAll_sensors() {

        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sensor;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfei") + (rs.getString("in_sensor").equalsIgnoreCase("true") ? "inSensor" : "outSensor"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_sensors_of_SFEI(String fk_sfei) {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sensor WHERE fk_sfei='" + fk_sfei + "';";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfei") + (rs.getString("in_sensor").equalsIgnoreCase("true") ? "inSensor" : "outSensor"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
