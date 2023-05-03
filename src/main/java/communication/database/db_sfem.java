package communication.database;

import communication.database.interfaces.I_SFEx;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class db_sfem implements I_SFEx {

    /**
     * Singleton pattern
     */
    public db_sfem() {
    }

    public static db_sfem getInstance() {
        return db_sfem.db_sfemHolder.INSTANCE;
    }

    private static class db_sfemHolder {
        private static final db_sfem INSTANCE = new db_sfem();
    }

    @Override
    public int insert(String sfem_name, String sf_configuration) {
        try {
            String def_vars = "SET @name = '" + sfem_name + "'," +
                    " @fk = '" + sf_configuration + "';";
            String query = "INSERT INTO sfem (name,fk_sf_configuration)" +
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
    public void delete(String sfem_name, String sf_configuration) {
        try {
            String query = "DELETE FROM sfem WHERE name ='" + sfem_name + "' AND fk_sf_configuration='" + sf_configuration + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_sfem_name, String new_sfem_name, String sf_configuration) {
        try {
            String query = "UPDATE sfem " +
                    "SET name = '" + new_sfem_name + "'" +
                    "WHERE name = '" + old_sfem_name + "' AND fk_sf_configuration='" + sf_configuration + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFEx() {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfem;";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sf_configuration"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFExFrom(String sf_configuration) {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfem WHERE fk_sf_configuration='" + sf_configuration + "';";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sf_configuration"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
