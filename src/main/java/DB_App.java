import communication.database.inbound_orders;
import communication.database.sf_configuration;
import controllers.production.cSFEM_production;
import controllers.warehouse.cSFEM_warehouse;
import models.SFEx_particular.SFEM_warehouse;
import utility.serialize.serializer;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DB_App {
    public static void main(String[] args) {

        try {
/*            sf_configuration sf_config = new sf_configuration();
            System.out.println(sf_config.insert("test"));
            System.out.println(sf_config.insert("test1"));

            System.out.println(Arrays.toString(sf_config.getAll_sf_configurations().toArray()));*/

//            inbound_orders.getInstance().insert(4,5,6);

            SFEM_warehouse sfemWarehouse = new SFEM_warehouse();

            cSFEM_warehouse  cSFEMWarehouse = new cSFEM_warehouse(sfemWarehouse);
            cSFEMWarehouse.init("sfee_warehouse");

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(cSFEMWarehouse, 0, 100, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}