package communication.database.mediators;

import communication.database.dbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class M_sfee extends queries_buffer implements IM_SFEx {

    public M_sfee() {
    }


    @Override
    public void insert(String sfee_name, String fk_sfem) {
        try {

            String query = "INSERT INTO sfee (name,fk_sfem)" +
                    "VALUES ('" + sfee_name + "','" + fk_sfem + "')" +
                    "ON DUPLICATE KEY UPDATE" +
                    "   name = '" + sfee_name + "';";
            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String sfee_name, String fk_sfem) {
        try {
            String query = "DELETE FROM sfee WHERE name ='" + sfee_name + "' AND fk_sf_configuration='" + fk_sfem + "';";
            getStoredQueries().add(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String old_sfee_name, String new_sfee_name, String fk_sfem) {
        try {
            String query = "UPDATE sfee " +
                    "SET name = '" + new_sfee_name + "'" +
                    "WHERE name = '" + old_sfee_name + "' AND fk_sf_configuration='" + fk_sfem + "';";
            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_SFEx() {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM sfee;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
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
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " of" + rs.getString("fk_sfem"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
