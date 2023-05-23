package communication.database.mediators;

import communication.database.dbConnection;
import models.inboundOrder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class M_outbound_orders extends queries_buffer implements IM_outbound_order {

    public M_outbound_orders() {
    }

    @Override
    public void insert() {
        try {
            String query = "INSERT INTO outbound_orders (order_date)" +
                    "VALUES (current_timestamp());";
            // Nothing on update because this table PK is serial, so there are no repetitions
            getStoredQueries().add(query);
//            return dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            String query = "DELETE FROM outbound_orders WHERE id =" + id + ";";
            getStoredQueries().add(query);
//            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAll_outbound_orders() {
        try {
            List<String> list = new ArrayList<>();
            String query = "SELECT * FROM outbound_orders;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("id") + ", " + rs.getString("order_date"));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
