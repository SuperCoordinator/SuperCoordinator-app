package communication.database.mediators;

import communication.database.dbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class M_sensor extends queries_buffer implements IM_sensor {

    public M_sensor() {
    }

    @Override
    public void insert(String name, String fk_sfei, boolean inSensor) {
        try {

            String query = "INSERT INTO sensor (name,fk_sfei,in_sensor)" +
                    "VALUES ('" + name + "','" + fk_sfei + "'," + inSensor + ")" +
                    "ON DUPLICATE KEY UPDATE" +
                    "   name = '" + name + "'," +
                    "   fk_sfei = '" + fk_sfei + "'," +
                    "   in_sensor = " + inSensor + ";";
            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String name, String fk_sfei) {

        try {
            String query = "DELETE FROM sensor WHERE name ='" + name + "' AND fk_sfei ='" + fk_sfei + "';";
            getStoredQueries().add(query);

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
    public List<String> getAll_sensorsFrom(String fk_sfei) {
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
