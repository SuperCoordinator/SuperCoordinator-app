package communication.database;

import communication.database.interfaces.I_production_history;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class db_production_history implements I_production_history {

    /**
     * Singleton pattern
     */
    public db_production_history() {
    }

    public static db_production_history getInstance() {
        return db_production_history.db_production_historyHolder.INSTANCE;
    }

    private static class db_production_historyHolder {
        private static final db_production_history INSTANCE = new db_production_history();
    }

    @Override
    public int insert(int fk_part_id, String fk_sensor_name, String material, String form) {
        try {
            String def_vars = "SET @fk_part = " + fk_part_id + "," +
                    "@fk_sensor = '" + fk_sensor_name + "'," +
                    "@material = '" + material + "'," +
                    "@form = '" + form + "'," +
                    "@time = current_timestamp(); ";

            String query = "INSERT INTO production_history (fk_part_id,fk_sensor_name,material,form,time_stamp)" +
                    "VALUES (@fk_part,@fk_sensor,@material,@form,@time)" +
                    "ON DUPLICATE KEY UPDATE" +
                    " material = @material," +
                    " form = @form," +
                    " time_stamp=@time;";

            Statement st = dbConnection.getConnection().createStatement();
            st.addBatch(def_vars);
            st.addBatch(query);

            return st.executeBatch()[1];

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int part_id) {
        try {
            String query = "DELETE FROM production_history WHERE fk_part_id = " + part_id + ";";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getProduction_History() {
        try {

            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM production_history;";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
/*                list.add(rs.getTimestamp("time_stamp").toString() + " " +
                        rs.getString("material") + " " +
                        rs.getString("form") + " " +
                        rs.getInt("fk_part_id") + " " +
                        rs.getString("fk_sensor_name"));*/
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
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
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
