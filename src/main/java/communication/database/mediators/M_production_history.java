package communication.database.mediators;

import communication.database.dbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class M_production_history extends queries_buffer implements IM_production_history {

    public M_production_history() {
    }
        @Override
    public void insert(int fk_part_id, String fk_sensor_name, String material, String form, Instant timestamp) {
        try {

            if (fk_part_id >= Integer.MAX_VALUE / 2)
                return;

            String query = "INSERT INTO production_history (fk_part_id,fk_sensor_name,material,form,time_stamp)" +
                    "VALUES (" + fk_part_id + ",'" + fk_sensor_name + "','" + material + "','" + form + "','" + Timestamp.from(timestamp) + "') " +
                    "ON DUPLICATE KEY UPDATE" +
                    " material = '" + material + "'," +
                    " form = '" + form + "'," +
                    " time_stamp = '" + Timestamp.from(timestamp) + "';";

            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int part_id) {
        try {
            String query = "DELETE FROM production_history WHERE fk_part_id = " + part_id + ";";
            getStoredQueries().add(query);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getProduction_History() {
        try {

            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM production_history;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                String row = "";
                for (int i = 0; i < 5; i++) {
                    row = row.concat(rs.getString(i + 1) + " ");
                }
                list.add(row);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getProduction_History_of(int part_id) {
        try {

            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM production_history WHERE fk_part_id= " + part_id + ";";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                String row = "";
                for (int i = 0; i < 5; i++) {
                    row = row.concat(rs.getString(i + 1) + " ");
                }
                list.add(row);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
