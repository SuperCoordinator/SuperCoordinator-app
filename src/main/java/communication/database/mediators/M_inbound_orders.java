package communication.database.mediators;

import communication.database.dbConnection;
import models.inboundOrder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class M_inbound_orders extends queries_buffer implements IM_inbound_orders {

    public M_inbound_orders() {
    }

    @Override
    public void insert(int metal_qty, int green_qty, int blue_qty) {
        try {

            String query = "INSERT INTO inbound_orders (order_date,metal_qty,green_qty,blue_qty)" +
                    "VALUES (current_timestamp()," + metal_qty + "," + green_qty + "," + blue_qty + ");";
            // Nothing on update because this table PK is serial, so there are no repetitions
            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            String query = "DELETE FROM inbound_orders WHERE id =" + id + ";";
            getStoredQueries().add(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(int id, int metal_qty, int green_qty, int blue_qty) {
        try {
            String query = "UPDATE inbound_orders " +
                    "SET metal_qty = " + metal_qty + ", green_qty = " + green_qty + ", blue_qty = " + blue_qty +
                    "WHERE id = " + id + ";";
            getStoredQueries().add(query);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<inboundOrder> getAll_inbound_orders() {
        try {
            List<inboundOrder> list = new ArrayList<>();
            String query = "SELECT * FROM inbound_orders;";
            ResultSet rs = dbConnection.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                list.add(new inboundOrder(rs.getInt("id"), rs.getInt("metal_qty"), rs.getInt("green_qty"), rs.getInt("blue_qty")));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
