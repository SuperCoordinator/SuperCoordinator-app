import controllers.cSFEM_production;
import controllers.cSFEM_transport;
import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_transport;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {

        SFEM_production newSFEM = new SFEM_production("SFEM_production test");

        cSFEM_production sfemController = new cSFEM_production(newSFEM);
        sfemController.init_SFEEs();
        sfemController.init_SFEE_controllers();
//        sfemController.firstRun(false);
//        sfemController.setupFailureMode();


        sfemController.openConnections();

        SFEM_transport newSFEM_transp = new SFEM_transport("SFEM_transport test");
        cSFEM_transport sfemTransportController = new cSFEM_transport(newSFEM_transp);
        sfemTransportController.init_SFEE_transport();
        sfemTransportController.init_SFEE_transport_controller(
                sfemController.searchMBbySFEE(newSFEM.getSFEEs().get(0).getName()),
                sfemController.searchMBbySFEE(newSFEM.getSFEEs().get(1).getName()),
                newSFEM.getSFEEs().get(0),
                newSFEM.getSFEEs().get(1));





        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
            scheduler.scheduleAtFixedRate(sfemController, 0, 100, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(sfemTransportController, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
