package communication.database.mediators;

import models.inboundOrder;

import java.util.List;

public interface IM_inbound_orders {

    int insert(int metal_qty, int green_qty, int blue_qty);

    void delete(int id);

    void update(int id,int metal_qty, int green_qty, int blue_qty );

    List<inboundOrder> getAll_inbound_orders();
}
