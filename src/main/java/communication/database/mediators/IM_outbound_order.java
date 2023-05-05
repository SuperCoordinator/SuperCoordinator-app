package communication.database.mediators;

import models.inboundOrder;

import java.util.List;

public interface IM_outbound_order {

    void insert();
    void delete(int id);

//    void update(int id, int metal_qty, int green_qty, int blue_qty);

    List<String> getAll_outbound_orders();
}
