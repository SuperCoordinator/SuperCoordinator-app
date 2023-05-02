package communication.database;

import communication.database.interfaces.I_sf_distribution;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class db_sf_distribution implements I_sf_distribution {

    /**
     * Singleton pattern
     */
    public db_sf_distribution() {
    }

    public static db_sf_distribution getInstance() {
        return db_sf_distribution.sf_configurationHolder.INSTANCE;
    }

    private static class sf_configurationHolder {
        private static final db_sf_distribution INSTANCE = new db_sf_distribution();
    }
    @Override
    public int insert(String sf_distribution) {
        try {
            String def_vars = "SET @name = '" + sf_distribution + "'," +
                    " @time = current_timestamp();";
            String query = "INSERT INTO sf_distribution (name,time_stamp)" +
                    "VALUES (@name,@time)" +
                    "ON DUPLICATE KEY UPDATE" +
                    "   name = @name," +
                    "   time_stamp = @time;";

            Statement st = dbConnection.getConnection().createStatement();
            st.addBatch(def_vars);
            st.addBatch(query);

            return st.executeBatch()[1];

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String sf_config_name) {

        try {
            String query = "DELETE FROM sf_distribution WHERE name ='" + sf_config_name + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_sf_config_name, String new_sf_config_name) {

        try {
            String query = "UPDATE sf_distribution " +
                    "SET name = '" + new_sf_config_name + "' " +
                    "WHERE name = '" + old_sf_config_name + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
    @Override
    public List<String> getAll_sf_configurations() {

        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sf_distribution;";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
