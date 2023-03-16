import controllers.SFEM_controller;
import models.SFEM;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {

        ArrayList<SFEM> SFEMs = new ArrayList<>();
        SFEM newSFEM = new SFEM("SFEM_test");

        SFEM_controller sfemController = new SFEM_controller(newSFEM);
        sfemController.init();

        sfemController.firstRun(false);
        sfemController.setupFailureMode();

        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(sfemController, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
