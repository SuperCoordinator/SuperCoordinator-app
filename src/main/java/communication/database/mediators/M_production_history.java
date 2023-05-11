package communication.database.mediators;

import communication.database.dbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class M_production_history extends queries_buffer implements IM_production_history {

    /**
     * Singleton pattern
     */
    public M_production_history() {
    }

    //
//    public static M_production_history getInstance() {
//        return M_production_history.db_production_historyHolder.INSTANCE;
//    }
//
//    private static class db_production_historyHolder {
//        private static final M_production_history INSTANCE = new M_production_history();
//    }
    @Override
    public void insert(int fk_part_id, String fk_sensor_name, String material, String form) {
        try {
//            String def_vars = "SET @fk_part = " + fk_part_id + "," +
//                    "@fk_sensor = '" + fk_sensor_name + "'," +
//                    "@material = '" + material + "'," +
//                    "@form = '" + form + "'," +
//                    "@time = current_timestamp(); ";
            if (fk_part_id < 0)
                return;

            String query = "INSERT INTO production_history (fk_part_id,fk_sensor_name,material,form,time_stamp)" +
                    "VALUES (" + fk_part_id + ",'" + fk_sensor_name + "','" + material + "','" + form + "',current_timestamp())" +
                    "ON DUPLICATE KEY UPDATE" +
                    " material = '" + material + "'," +
                    " form = '" + form + "'," +
                    " time_stamp= current_timestamp();";

            getStoredQueries().add(query);
//            Statement st = dbConnection.getConnection().createStatement();
//            st.addBatch(def_vars);
//            st.addBatch(query);
//
//            return st.executeBatch()[1];

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int part_id) {
        try {
            String query = "DELETE FROM production_history WHERE fk_part_id = " + part_id + ";";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();

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
