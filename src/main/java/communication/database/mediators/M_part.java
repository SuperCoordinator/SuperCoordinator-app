package communication.database.mediators;

import communication.database.dbConnection;
import models.base.part;
import models.partDescription;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class M_part extends queries_buffer implements IM_part {

    /**
     * Singleton pattern
     */
    public M_part() {
    }

    //
//    public static M_part getInstance() {
//        return partHolder.INSTANCE;
//    }
//
//    private static class partHolder {
//        private static final M_part INSTANCE = new M_part();
//    }
    @Override
    public void insert(int id, String sf_configuration, String status, int inbound_order) {
        try {
//            String def_vars = "SET @id = " + id + "," +
//                    " @fk_sf_configuration = '" + sf_configuration + "'," +
//                    " @status = '" + status + "'," +
//                    " @fk_inbound_order = " + inbound_order + ";";

            if (id < 0)
                return;

            String query = "INSERT INTO part (id,fk_sf_configuration,status,fk_inbound_orders) " +
                    "SELECT part_id, sf_config, st, in_order " +
                    "FROM (SELECT '" + id + "' as part_id, '" + sf_configuration + "' as sf_config, '" + status + "' as st, '" + inbound_order + "' as in_order) temp " +
                    "WHERE EXISTS (SELECT id FROM inbound_orders WHERE id = " + inbound_order + ") " +
//                    "VALUES (@id,@fk_sf_configuration,@status,@fk_inbound_order) " +
                    "ON DUPLICATE KEY UPDATE " +
                    " fk_inbound_orders = " + inbound_order + ";";
            getStoredQueries().add(query);
//            Statement st = dbConnection.getInstance().getConnection().createStatement();
//            st.addBatch(def_vars);
//            st.addBatch(query);
//
//            return st.executeBatch()[1];
        } catch (
                Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void delete(int id, String sf_configuration) {
        try {
            if (id < 0)
                return;
            String query = "DELETE FROM part WHERE id =" + id + " AND fk_sf_configuration='" + sf_configuration + "';";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update_status(int id, String sf_configuration, String status) {
        try {
            if (id < 0)
                return;
            String query = "UPDATE part " +
                    "SET status = '" + status + "' " +
                    "WHERE id = " + id + " AND fk_sf_configuration='" + sf_configuration + "';";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update_outboundOrder(int id, String sf_configuration, int outbound_order) {
        try {
            if(id < 0)
                return;
            String query = "UPDATE part " +
                    "SET fk_outbound_orders = " +
                    "(SELECT id FROM outbound_orders WHERE id = " + outbound_order + ") " +
                    "WHERE part.id = " + id + " AND part.fk_sf_configuration = '" + sf_configuration + "';";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<models.base.part> getAll_parts(String sf_configuration) {
        try {
            List<models.base.part> list = new ArrayList<>();
            String query = "SELECT * FROM part;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                part.status st = null;
                for (part.status s : part.status.values()) {
                    if (s.toString().equalsIgnoreCase(rs.getString("status"))) {
                        st = s;
                        break;
                    }
                }
                // In order to have the last partDescription
                String query2 = "SELECT * FROM part " +
                        "INNER JOIN production_history " +
                        "WHERE part.id = " + rs.getInt("id") + " " +
                        "AND production_history.fk_part_id = part.id " +
                        "ORDER BY time_stamp DESC LIMIT 1;";
                ResultSet rs2 = dbConnection.getInstance().getConnection().prepareStatement(query2).executeQuery();
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
//                    list.add(new part());
                    System.out.println("NOT SUPPOSED TO ENTER HERE! \nPart " + rs.getInt("id") + " do not have record in production_history table.");
                }

            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<models.base.part> getAllPartsNotShipped(String sf_configuration) {
        try {
            List<models.base.part> list = new ArrayList<>();
            String query = "SELECT * FROM part WHERE fk_outbound_orders is NULL;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                part.status st = null;
                for (part.status s : part.status.values()) {
                    if (s.toString().equalsIgnoreCase(rs.getString("status"))) {
                        st = s;
                        break;
                    }
                }
                // In order to have the last partDescription
                String query2 = "SELECT * FROM part " +
                        "INNER JOIN production_history " +
                        "WHERE part.id = " + rs.getInt("id") + " " +
                        "AND production_history.fk_part_id = part.id " +
                        "ORDER BY time_stamp DESC LIMIT 1;";
                ResultSet rs2 = dbConnection.getInstance().getConnection().prepareStatement(query2).executeQuery();
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
//                    list.add(new part());
                    System.out.println("NOT SUPPOSED TO ENTER HERE! \nPart " + rs.getInt("id") + " do not have record in production_history table.");
                }

            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
