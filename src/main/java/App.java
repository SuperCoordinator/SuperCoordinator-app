import controllers.SFEM_production_controller;
import models.SFEx_particular.SFEM_production;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {

        ArrayList<SFEM_production> SFEMs = new ArrayList<>();
        SFEM_production newSFEM = new SFEM_production("SFEM_test");

        SFEM_production_controller sfemController = new SFEM_production_controller(newSFEM);
        sfemController.init_SFEEs();
        sfemController.init_SFEE_controllers();
//        sfemController.firstRun(false);
//        sfemController.setupFailureMode();

        sfemController.openConnections();


        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(sfemController, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
