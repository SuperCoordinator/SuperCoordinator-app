package communication.database;

import communication.database.interfaces.I_SFEx;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class db_sfei implements I_SFEx {

    /**
     * Singleton pattern
     */
    public db_sfei() {
    }

    public static db_sfei getInstance() {
        return db_sfei.db_sfeiHolder.INSTANCE;
    }

    private static class db_sfeiHolder {
        private static final db_sfei INSTANCE = new db_sfei();
    }
    @Override
    public int insert(String sfei_name, String fk_sfee) {
        try {
            String def_vars = "SET @name = '" + sfei_name + "'," +
                    " @fk = '" + fk_sfee + "';";
            String query = "INSERT INTO sfei (name,fk_sfee)" +
                    "VALUES (@name,@fk)" +
                    "ON DUPLICATE KEY UPDATE" +
                    "   name = @name;";

            Statement st = dbConnection.getConnection().createStatement();
            st.addBatch(def_vars);
            st.addBatch(query);

            return st.executeBatch()[1];

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String sfei_name, String fk_sfee) {
        try {
            String query = "DELETE FROM sfei WHERE name ='" + sfei_name + "' AND fk_sfee='" + fk_sfee + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_sfei_name, String new_sfei_name, String fk_sfee) {
        try {
            String query = "UPDATE sfei " +
                    "SET name = '" + new_sfei_name + "'" +
                    "WHERE name = '" + old_sfei_name + "' AND fk_sf_configuration='" + fk_sfee + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFEx() {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfei;";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfee"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFExFrom(String fk_sfee) {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfei WHERE fk_sfee='" + fk_sfee + "';";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfee"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
