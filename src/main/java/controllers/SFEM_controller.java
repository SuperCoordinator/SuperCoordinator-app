package controllers;

import models.SFEE;
import models.SFEM;
import monitor.SFEE_monitor;
import monitor.SFEM_monitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SFEM_controller {

    private final SFEM sfem;

    private SFEM_monitor sfemMonitor;

    ArrayList<SFEE_controller> sfeeControllers;
    private final viewers.SFEM viewer;

    public SFEM_controller(SFEM sfem) {
        this.sfem = sfem;

        this.sfeeControllers = new ArrayList<>();
        this.viewer = new viewers.SFEM();

    }

    public void init() {

        try {
            // # of SFEE to be added
            //String input = viewer.nSFEE();
            String input = "1";
            for (int i = 0; i < Integer.parseInt(input); i++) {

                //String[] inputs = viewer.SFEE_params(i);
                String[] inputs = {"CMC", "1", "1"};

                SFEE sfee = new SFEE(
                        inputs[0],
                        Integer.parseInt(inputs[1]) == 1 ? SFEE.communication.MODBUS : SFEE.communication.OPC_UA,
                        Integer.parseInt(inputs[2]) == 1 ? SFEE.SFEE_type.SIMULATION : SFEE.SFEE_type.REAL);

                sfem.addNewSFEE(sfee);

                SFEE_controller sfeeController = new SFEE_controller(sfee);
                sfeeController.init();

                sfeeControllers.add(sfeeController);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sfemMonitor = new SFEM_monitor(sfem);
    }

    public void firstRun() {

        for (SFEE_controller sfeeController : sfeeControllers) {
            sfeeController.launchSetup();
        }

    }

    public void start() {

        System.out.print("Press ENTER to start simulation");
        Scanner in = new Scanner(System.in);
        in.nextLine();

        for (Map.Entry<Integer, SFEE> entry : sfem.getSFEEs().entrySet()) {
            entry.getValue().getMb().reOpenConnection();
        }

        for (SFEE_controller sfeeController : sfeeControllers) {
            sfeeController.startMonitoring();
        }

        // Launch monitor thread
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(sfemMonitor, 0, 100, TimeUnit.MILLISECONDS);

        // Launch simulation
        sfeeControllers.get(0).launchSimulation();

    }
}
