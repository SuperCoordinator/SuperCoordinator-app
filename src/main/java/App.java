import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_transport;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {

        SFEM_production SFEM1 = new SFEM_production("SFEM_production1");

        cSFEM_production sfem1Controller = new cSFEM_production(SFEM1);
        sfem1Controller.init_SFEEs(1);
        sfem1Controller.init_SFEE_controllers(3);

        SFEM_production SFEM2 = new SFEM_production("SFEM_production2");

        cSFEM_production sfem2Controller = new cSFEM_production(SFEM2);
        sfem2Controller.init_SFEEs(1);
        sfem2Controller.init_SFEE_controllers(4);
//        sfemController.firstRun(false);
//        sfemController.setupFailureMode();


        sfem1Controller.openConnections();
        sfem2Controller.openConnections();

        SFEM_transport newSFEM_transp = new SFEM_transport("SFEM_transport");
        cSFEM_transport sfemTransportController = new cSFEM_transport(newSFEM_transp);
        sfemTransportController.init_SFEE_transport();
        sfemTransportController.init_SFEE_transport_controller(
                sfem1Controller.searchMBbySFEE(SFEM1.getSFEEs().get(0).getName()),
                sfem2Controller.searchMBbySFEE(SFEM2.getSFEEs().get(0).getName()),
                SFEM1.getSFEEs().get(SFEM1.getSFEEs().size() - 1),
                SFEM2.getSFEEs().get(0));


        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
            scheduler.scheduleAtFixedRate(sfem1Controller, 0, 100, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(sfem2Controller, 0, 100, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(sfemTransportController, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
