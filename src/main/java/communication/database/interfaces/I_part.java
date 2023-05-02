package communication.database.interfaces;

import models.base.part;

import java.util.List;

public interface I_part {

    int insert(int id, String sf_distribution, String status, int inbound_order);

    void delete(int id, String sf_distribution);

    void update_status(int id, String sf_distribution, String status);

    void update_outboundOrder(int id, String sf_distribution, int outbound_order);

    List<part> getAll_parts(String sf_distribution);
}
