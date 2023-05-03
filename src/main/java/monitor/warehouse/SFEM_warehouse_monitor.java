package monitor.warehouse;

import communication.database.mediators.M_part;
import communication.database.mediators.M_production_history;
import communication.database.mediators.M_inbound_orders;
import models.base.part;
import models.inboundOrder;
import models.partDescription;
import utility.serialize.serializer;
import utility.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileReader;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SFEM_warehouse_monitor {

    private ArrayList<part> recentArrivedParts = new ArrayList<>();

    public ArrayList<part> getRecentArrivedParts() {
        return recentArrivedParts;
    }

    private Instant old_t;

    private int part_id;
    private int check_period;

    public SFEM_warehouse_monitor(int part_id_offset, int checkOrder_period_min) {
        this.part_id = 0;
        this.check_period = checkOrder_period_min;
        old_t = Instant.parse("2023-04-01T12:00:00.840857500Z");
    }

    public boolean loop() {

        try {
            if (Duration.between(old_t, Instant.now()).toMinutes() >= check_period) {
                receiveOrders();
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
            M_inbound_orders.getInstance().insert(order.getMetal_qty(), order.getGreen_qty(), order.getBlue_qty());

            createParts(order);

            file_index++;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createParts(inboundOrder order) {
        try {
            recentArrivedParts.clear();
            int m = order.getMetal_qty(), g = order.getGreen_qty(), b = order.getBlue_qty();
            while (m + g + b > 0) {
                int color = utils.getInstance().getRandom().nextInt(0, 3);
                part p = null;
                switch (color) {
                    case 0 -> {
                        p = new part(part_id, new partDescription(partDescription.material.METAL, partDescription.form.RAW));
                        recentArrivedParts.add(p);
                        part_id++;
                        m--;
                    }
                    case 1 -> {
                        p = new part(part_id, new partDescription(partDescription.material.GREEN, partDescription.form.RAW));
                        recentArrivedParts.add(p);
                        part_id++;
                        g--;
                    }
                    case 2 -> {
                        p = new part(part_id, new partDescription(partDescription.material.BLUE, partDescription.form.RAW));
                        recentArrivedParts.add(p);
                        part_id++;
                        b--;
                    }
                }
                M_part.getInstance().insert(Objects.requireNonNull(p).getId(),
                        serializer.getInstance().scene.toString(),
                        Objects.requireNonNull(p).getState().toString(),
                        M_inbound_orders.getInstance().getAll_inbound_orders().size());

                // register insertion in warehouse
                M_production_history.getInstance().insert(Objects.requireNonNull(p).getId(),
                        "warehouse_door",
                        Objects.requireNonNull(p).getReality().material().toString(),
                        Objects.requireNonNull(p).getReality().form().toString());

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clearStoredParts() {
        recentArrivedParts.clear();
    }

    public void loadWHBasedOnPrevStock() {
        List<part> prevStock = M_part.getInstance().getAll_parts(serializer.getInstance().scene.toString());
        part_id = prevStock.size();

        // remove the parts that was in production, those aren't possible to re-use
        // Only the one with IN_STOCK status
        prevStock.removeIf(part -> !part.getState().equals(models.base.part.status.IN_STOCK));

        recentArrivedParts.addAll(prevStock);

    }
}
