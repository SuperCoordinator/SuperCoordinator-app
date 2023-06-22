package communication.database.mediators;

import communication.database.dbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class M_sfem extends queries_buffer implements IM_SFEx {

    public M_sfem() {
    }

    @Override
    public void insert(String sfem_name, String sf_configuration) {
        try {

            String query = "INSERT INTO sfem (name,fk_sf_configuration)" +
                    "VALUES ('" + sfem_name + "','" + sf_configuration + "')" +
                    "ON DUPLICATE KEY UPDATE" +
                    "   name = '" + sfem_name + "';";
            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String sfem_name, String sf_configuration) {
        try {
            String query = "DELETE FROM sfem WHERE name ='" + sfem_name + "' AND fk_sf_configuration='" + sf_configuration + "';";
            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_sfem_name, String new_sfem_name, String sf_configuration) {
        try {
            String query = "UPDATE sfem " +
                    "SET name = '" + new_sfem_name + "'" +
                    "WHERE name = '" + old_sfem_name + "' AND fk_sf_configuration='" + sf_configuration + "';";
            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFEx() {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfem;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
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
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sf_configuration"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
