package communication.database.mediators;

import communication.database.dbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class M_sfei extends queries_buffer implements IM_SFEx {

    /**
     * Singleton pattern
     */
    public M_sfei() {
    }

    //
//    public static M_sfei getInstance() {
//        return M_sfei.db_sfeiHolder.INSTANCE;
//    }
//
//    private static class db_sfeiHolder {
//        private static final M_sfei INSTANCE = new M_sfei();
//    }
    @Override
    public void insert(String sfei_name, String fk_sfee) {
        try {
//            String def_vars = "SET @name = '" + sfei_name + "'," +
//                    " @fk = '" + fk_sfee + "';";
            String query = "INSERT INTO sfei (name,fk_sfee)" +
                    "VALUES ('" + sfei_name + "','" + fk_sfee + "')" +
                    "ON DUPLICATE KEY UPDATE" +
                    "   name = '" + sfei_name + "';";
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
    public void delete(String sfei_name, String fk_sfee) {
        try {
            String query = "DELETE FROM sfei WHERE name ='" + sfei_name + "' AND fk_sfee='" + fk_sfee + "';";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_sfei_name, String new_sfei_name, String fk_sfee) {
        try {
            String query = "UPDATE sfei " +
                    "SET name = '" + new_sfei_name + "'" +
                    "WHERE name = '" + old_sfei_name + "' AND fk_sf_configuration='" + fk_sfee + "';";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFEx() {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfei;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
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
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfee"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
