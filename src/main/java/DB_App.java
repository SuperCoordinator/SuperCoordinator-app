import communication.database.db_part;
import communication.database.db_production_history;
import controllers.warehouse.cSFEM_warehouse;
import models.SFEx_particular.SFEM_warehouse;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DB_App {
    public static void main(String[] args) {

        try {
//            db_sf_configuration sf_config = new db_sf_configuration();
//            System.out.println(sf_config.insert("test"));
//            System.out.println(sf_config.insert("test1"));
//
//            System.out.println(Arrays.toString(sf_config.getAll_inbound_orders().toArray()));
//
//            db_inbound_orders.getInstance().insert(4,5,6);

//            SFEM_warehouse sfemWarehouse = new SFEM_warehouse();
//
//            cSFEM_warehouse cSFEMWarehouse = new cSFEM_warehouse(sfemWarehouse, 1);
//            cSFEMWarehouse.init("sfee_warehouse");
//
//            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//            scheduler.scheduleAtFixedRate(cSFEMWarehouse, 0, 100, TimeUnit.MILLISECONDS);

//            System.out.println(Arrays.toString(db_production_history.getInstance().getProduction_History_of(1).toArray()));


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}