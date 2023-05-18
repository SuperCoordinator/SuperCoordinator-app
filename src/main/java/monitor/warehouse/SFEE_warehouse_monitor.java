package monitor.warehouse;


import communication.database.dbConnection;
import models.SFEx.SFEM_warehouse;
import models.base.SFEE;
import models.base.SFEI;
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SFEE_warehouse_monitor {

    private SFEE sfee;

    private SFEM_warehouse.warehouseOrganization whOrganization;
    private Instant old_t;

    private int part_id, inbound_order_id, outbound_order_id;
    private int check_period;

    public SFEE_warehouse_monitor(SFEE sfee, int checkOrder_period_min, SFEM_warehouse.warehouseOrganization warehouseOrganization) {
        this.sfee = sfee;
        this.part_id = 0;
        this.inbound_order_id = 0;
        this.outbound_order_id = 0;
        this.check_period = checkOrder_period_min;
        this.whOrganization = warehouseOrganization;
        old_t = Instant.parse("2023-04-01T12:00:00.840857500Z");
    }

    public boolean loop() {

        try {
            if (Duration.between(old_t, Instant.now()).toMinutes() >= check_period) {
                receiveOrders();
                dispatchOrders();
                old_t = Instant.now();
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void dispatchOrders() {

        try {
            SFEI sfei = sfee.getSFEIbyIndex(1);
            if (sfee.getSFEIbyIndex(1).getPartsATM().size() > 0) {
                // create outbound order
                dbConnection.getInstance().getOutbound_orders().insert();
                outbound_order_id++;
                Iterator<part> iterator = sfei.getPartsATM().iterator();
                while (iterator.hasNext()) {
                    part movingPart = iterator.next();
                    if (movingPart.getState().equals(part.status.PRODUCED)) {

                        //update in db fk to outbound order
                        dbConnection.getInstance().getParts().update_outboundOrder(
                                Objects.requireNonNull(movingPart).getId(),
                                serializer.getInstance().scene.toString(),
                                outbound_order_id);

                        // register insertion in warehouse
                        dbConnection.getInstance().getProduction_history().insert(
                                Objects.requireNonNull(movingPart).getId(),
                                "warehouse_expeditionDoor",
                                Objects.requireNonNull(movingPart).getReality().material().toString(),
                                Objects.requireNonNull(movingPart).getReality().form().toString(),
                                Instant.now());

                        iterator.remove();

                        /* Increment the number of parts moved by the SFEI sfei_idx. */
                        sfei.setnPiecesMoved(sfei.getnPiecesMoved() + 1);
                        /* DATABASE update nParts -> sfei table */
                        dbConnection.getInstance().getSfeis().update_nMovedParts(
                                sfei.getName(),
                                sfee.getName(),
                                sfei.getnPiecesMoved());
                    } else
                        throw new RuntimeException("This part is in the exit warehouse but is not produced");

                }

            }
        } catch (RuntimeException e) {
            e.printStackTrace();
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
//            dbConnection.getInstance().getInbound_orders().insert(inbound_order_id, order.getMetal_qty(), order.getGreen_qty(), order.getBlue_qty());
//            createParts(order, inbound_order_id);
//            inbound_order_id++;
            dbConnection.getInstance().getInbound_orders().insert(order.getMetal_qty(), order.getGreen_qty(), order.getBlue_qty());
            inbound_order_id++;
            createParts(order, inbound_order_id);
            file_index++;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createParts(inboundOrder order, int inboundOrder_id) {
        try {
            ArrayList<part> recentArrivedParts = new ArrayList<>();
            int m = order.getMetal_qty(), g = order.getGreen_qty(), b = order.getBlue_qty();
            while (m + g + b > 0) {
                int color = pickColor(m, g, b);
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
                dbConnection.getInstance().getParts().insert(Objects.requireNonNull(p).getId(),
                        serializer.getInstance().scene.toString(),
                        Objects.requireNonNull(p).getState().toString(),
                        inboundOrder_id);

                // register insertion in warehouse
                dbConnection.getInstance().getProduction_history().insert(
                        Objects.requireNonNull(p).getId(),
                        "warehouse_entryDoor",
                        Objects.requireNonNull(p).getReality().material().toString(),
                        Objects.requireNonNull(p).getReality().form().toString(),
                        Instant.now());

            }

            SFEI sfei = sfee.getSFEIs().get(0);
            sfei.getPartsATM().addAll(recentArrivedParts);
            System.out.println("#parts in the warehouse: " + sfee.getSFEIs().get(0).getPartsATM().size());

            /* Increment the number of parts moved by the SFEI sfei_idx. */
            sfei.setnPiecesMoved(sfei.getnPiecesMoved() + recentArrivedParts.size());
            /* DATABASE update nParts -> sfei table */
            dbConnection.getInstance().getSfeis().update_nMovedParts(
                    sfei.getName(),
                    sfee.getName(),
                    sfei.getnPiecesMoved());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int oldSelection = 2;

    private int pickColor(int m, int g, int b) {
        int selection = -1;
        if (Objects.requireNonNull(whOrganization) == SFEM_warehouse.warehouseOrganization.SEQUENTIAL) {
            switch (oldSelection) {
                case 2 -> {
                    if (m > 0)
                        selection = 0;
                    else if (g > 0)
                        selection = 1;
                    else
                        selection = oldSelection;
                }
                case 0 -> {
                    if (g > 0)
                        selection = 1;
                    else if (b > 0)
                        selection = 2;

                }
                case 1 -> {
                    if (b > 0)
                        selection = 2;
                    else if (m > 0)
                        selection = 0;
                    else
                        selection = oldSelection;
                }
            }
        } else {
            // RANDOM
            do {
                selection = utils.getInstance().getRandom().nextInt(0, 3);
            } while ((selection == 0 && m == 0) || (selection == 1 && g == 0) || (selection == 2 && b == 0));
        }
        oldSelection = selection;
        return selection;
    }

    public void loadWHBasedOnPrevStock() {
        try {
            // Establish the inbound order id
            inbound_order_id = Objects.requireNonNull(dbConnection.getInstance()).getInbound_orders().getAll_inbound_orders().size();

            // Establish the outbound order id
            outbound_order_id = Objects.requireNonNull(dbConnection.getInstance()).getOutbound_orders().getAll_outbound_orders().size();

            List<part> prevStock = Objects.requireNonNull(dbConnection.getInstance()).getParts().getAll_parts(serializer.getInstance().scene.toString());
            part_id = prevStock.size();

            // remove the parts that was in production, those aren't possible to re-use
            // Only the one with IN_STOCK status
            prevStock.removeIf(part -> !part.getState().equals(models.base.part.status.IN_STOCK));
            sfee.getSFEIs().get(0).getPartsATM().addAll(prevStock);

            // Get the parts that are not shipped
            List<part> partsNotShipped = Objects.requireNonNull(dbConnection.getInstance()).getParts().getAllPartsNotShipped(serializer.getInstance().scene.toString());
            // Remove the parts that are not PRODUCED
            partsNotShipped.removeIf(part -> !part.getState().equals(models.base.part.status.PRODUCED));
            sfee.getSFEIs().get(1).getPartsATM().addAll(partsNotShipped);

            System.out.println("#parts in the entry warehouse: " + sfee.getSFEIs().get(0).getPartsATM().size());
            System.out.println("#parts in the exit warehouse: " + sfee.getSFEIs().get(1).getPartsATM().size());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
