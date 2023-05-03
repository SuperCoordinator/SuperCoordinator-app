package communication.database;

import communication.database.interfaces.I_SFEx;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class db_sfee implements I_SFEx {

    /**
     * Singleton pattern
     */
    public db_sfee() {
    }

    public static db_sfee getInstance() {
        return db_sfee.db_sfeeHolder.INSTANCE;
    }

    private static class db_sfeeHolder {
        private static final db_sfee INSTANCE = new db_sfee();
    }

    @Override
    public int insert(String sfee_name, String fk_sfem) {
        try {
            String def_vars = "SET @name = '" + sfee_name + "'," +
                    " @fk = '" + fk_sfem + "';";
            String query = "INSERT INTO sfee (name,fk_sfem)" +
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
    public void delete(String sfee_name, String fk_sfem) {
        try {
            String query = "DELETE FROM sfee WHERE name ='" + sfee_name + "' AND fk_sf_configuration='" + fk_sfem + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_sfee_name, String new_sfee_name, String fk_sfem) {
        try {
            String query = "UPDATE sfee " +
                    "SET name = '" + new_sfee_name + "'" +
                    "WHERE name = '" + old_sfee_name + "' AND fk_sf_configuration='" + fk_sfem + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFEx() {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfee;";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfem"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFExFrom(String fk_sfem) {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfee WHERE fk_sfem='" + fk_sfem + "';";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfem"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
