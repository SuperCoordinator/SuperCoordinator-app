package communication.database;

import communication.database.interfaces.I_sensor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class db_sensor implements I_sensor {

    /**
     * Singleton pattern
     */
    public db_sensor() {
    }

    public static db_sensor getInstance() {
        return db_sensor.db_sensorHolder.INSTANCE;
    }

    private static class db_sensorHolder {
        private static final db_sensor INSTANCE = new db_sensor();
    }

    @Override
    public int insert(String name, String fk_sfei, boolean inSensor) {
        try {
            String def_vars = "SET @name = '" + name + "'," +
                    " @fk = '" + fk_sfei + "'," +
                    " @inSensor = " + inSensor + " ;";
            String query = "INSERT INTO sensor (name,fk_sfei,in_sensor)" +
                    "VALUES (@name,@fk,@inSensor)" +
                    "ON DUPLICATE KEY UPDATE" +
                    "   name = @name," +
                    "   fk_sfei = @fk," +
                    "   in_sensor = @inSensor;";

            Statement st = dbConnection.getConnection().createStatement();
            st.addBatch(def_vars);
            st.addBatch(query);

            return st.executeBatch()[1];

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String name, String fk_sfei) {

        try {
            String query = "DELETE FROM sensor WHERE name ='" + name + "' AND fk_sfei ='" + fk_sfei + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_name, String new_name, String fk_sfei) {

        try {
            String query = "UPDATE sensor " +
                    "SET name = '" + new_name + "'" +
                    "WHERE name = '" + old_name + "' AND fk_sf_distribution='" + fk_sfei + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public List<String> getAll_sensors() {

        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sensor;";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
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
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfei") + (rs.getString("in_sensor").equalsIgnoreCase("true") ? "inSensor" : "outSensor"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
