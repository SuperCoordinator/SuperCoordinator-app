package communication.database;

import communication.database.interfaces.I_part;
import models.base.part;
import models.partDescription;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class db_part implements I_part {

    /**
     * Singleton pattern
     */
    public db_part() {
    }

    public static db_part getInstance() {
        return db_part.partHolder.INSTANCE;
    }

    private static class partHolder {
        private static final db_part INSTANCE = new db_part();
    }


    @Override
    public int insert(int id, String sf_distribution, String status, int inbound_order) {
        try {
            String def_vars = "SET @id = " + id + "," +
                    " @fk_sf_distribution = '" + sf_distribution + "'," +
                    " @status = '" + status + "'," +
                    " @fk_inbound_order = " + inbound_order + ";";

            String query = "INSERT INTO part (id,fk_sf_distribution,status,fk_inbound_orders) " +
                    "VALUES (@id,@fk_sf_distribution,@status,@fk_inbound_order) " +
                    "ON DUPLICATE KEY UPDATE" +
                    "   fk_inbound_orders = @fk_inbound_order;";

            Statement st = dbConnection.getConnection().createStatement();
            st.addBatch(def_vars);
            st.addBatch(query);

            return st.executeBatch()[1];

/*            String query = "INSERT INTO part (id,fk_sf_distribution,status,fk_inbound_orders)" +
                    "VALUES (" + id + ",'" + db_sf_distribution + "'," + "'" + status + "'," + inbound_order + ");";
            return dbConnection.getConnection().prepareStatement(query).executeUpdate();*/
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id, String sf_distribution) {
        try {
            String query = "DELETE FROM part WHERE id =" + id + " AND fk_sf_distribution='" + sf_distribution + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update_status(int id, String sf_distribution, String status) {
        try {
            String query = "UPDATE part " +
                    "SET status = '" + status + "'" +
                    "WHERE id = " + id + " AND fk_sf_distribution='" + sf_distribution + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update_outboundOrder(int id, String sf_distribution, int outbound_order) {
        try {
            String query = "UPDATE part " +
                    "SET fk_outbound_orders = '" + outbound_order + "'" +
                    "WHERE id = " + id + " AND fk_sf_distribution='" + sf_distribution + "';";
            dbConnection.getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<models.base.part> getAll_parts(String sf_distribution) {
        try {
            List<models.base.part> list = new ArrayList<>();
            String query = "SELECT * FROM part;";
            ResultSet rs = dbConnection.getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                part.status st = null;
                for (part.status s : part.status.values()) {
                    if (s.toString().equalsIgnoreCase(rs.getString("status"))) {
                        st = s;
                        break;
                    }
                }
                // In order to have the last partDescription
                String query2 = "SELECT * FROM part INNER JOIN production_history WHERE part.id = " + rs.getInt("id") + " AND production_history.fk_part_id = part.id ORDER BY time_stamp DESC LIMIT 1;";
                ResultSet rs2 = dbConnection.getConnection().prepareStatement(query2).executeQuery();
                if (rs2.next()) {
                    partDescription.material mat = null;
                    for (partDescription.material m : partDescription.material.values()) {
                        if (m.toString().equalsIgnoreCase(rs2.getString("material"))) {
                            mat = m;
                            break;
                        }
                    }
                    partDescription.form form = null;
                    for (partDescription.form f : partDescription.form.values()) {
                        if (f.toString().equalsIgnoreCase(rs2.getString("form"))) {
                            form = f;
                            break;
                        }
                    }

                    list.add(new models.base.part(
                            rs.getInt("id"),
                            Objects.requireNonNull(st),
                            new partDescription(Objects.requireNonNull(mat), Objects.requireNonNull(form))));
                } else {
                    // Not found in query2, so the part was in the warehouse
                    // it is necessary more information to create the correct part
                    list.add(new part());
                }

            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
