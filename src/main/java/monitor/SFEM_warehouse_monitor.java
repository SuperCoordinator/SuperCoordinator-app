package monitor;

import communication.database.inbound_orders;
import models.base.part;
import models.inboundOrder;
import models.partDescription;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileReader;
import java.time.*;
import java.util.ArrayList;
import java.util.Objects;

public class SFEM_warehouse_monitor {

    private ArrayList<part> recentArrivedParts = new ArrayList<>();

    public ArrayList<part> getRecentArrivedParts() {
        return recentArrivedParts;
    }

    private Instant old_t;

    private int part_id;

    public SFEM_warehouse_monitor(int part_id_offset) {
        this.part_id = part_id_offset;
        old_t = Instant.parse("2023-04-01T12:00:00.840857500Z");
    }

    public boolean loop() {

        try {
            if (Duration.between(old_t, Instant.now()).toMinutes() >= 1) {
                receiveOrders();
//                System.out.println("#parts in stock:" + recentArrivedParts.size());
                old_t = Instant.now();
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private int file_index = 0;

    private void receiveOrders() {
        try {
            File f = new File("src/main/resources/inboundOrders");
            if (file_index == Objects.requireNonNull(f.list()).length) {
                file_index = 0;
            }

            JAXBContext context = JAXBContext.newInstance(inboundOrder.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            inboundOrder order = (inboundOrder) unmarshaller.unmarshal(new FileReader(f.getPath() + "/" + Objects.requireNonNull(f.list())[file_index]));

            // Register received order on DB
            inbound_orders.getInstance().insert(order.getMetal_qty(), order.getGreen_qty(), order.getBlue_qty());

            createParts(order);

            file_index++;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createParts(inboundOrder order) {
        try {
            recentArrivedParts.clear();
            for (int i = 0; i < order.getMetal_qty(); i++) {
                // partID ?? - get from DB ?
                recentArrivedParts.add(new part(part_id, new partDescription(partDescription.material.METAL, partDescription.form.RAW)));
                part_id++;
            }
            for (int i = 0; i < order.getGreen_qty(); i++) {
                // partID ?? - get from DB ?
                recentArrivedParts.add(new part(part_id, new partDescription(partDescription.material.GREEN, partDescription.form.RAW)));
                part_id++;
            }
            for (int i = 0; i < order.getBlue_qty(); i++) {
                // partID ?? - get from DB ?
                recentArrivedParts.add(new part(part_id, new partDescription(partDescription.material.BLUE, partDescription.form.RAW)));
                part_id++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clearStoredParts() {
        recentArrivedParts.clear();
    }
}
