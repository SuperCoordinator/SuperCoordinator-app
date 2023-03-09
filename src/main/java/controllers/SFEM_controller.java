package controllers;

import communication.modbus;
import models.SFEE;
import models.SFEM;
import monitor.SFEM_monitor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SFEM_controller implements Runnable {

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
            String input = "2";
            for (int i = 0; i < Integer.parseInt(input); i++) {
                modbus mb = null;

                //String[] inputs = viewer.SFEE_params(i);
                String[] inputs = {"sfee" + i, "1", "1"};

                /* QUESTAO DO SLAVE ID*/
                String[] comConfig = viewer.setupComunication(0);
                //String[] comConfig = {"192.168.240.1", "502", String.valueOf(i + 1)};
                if (Integer.parseInt(inputs[1]) == 1) {
                    mb = searchForOpenConnections(comConfig);
                }
                System.out.println(mb);
                SFEE sfee = new SFEE(
                        inputs[0],
                        Integer.parseInt(inputs[2]) == 1 ? SFEE.SFEE_type.SIMULATION : SFEE.SFEE_type.REAL);

                sfem.addNewSFEE(sfee);

                SFEE_controller sfeeController = new SFEE_controller(
                        sfee,
                        Integer.parseInt(inputs[1]) == 1 ? SFEE_controller.communicationOption.MODBUS : SFEE_controller.communicationOption.OPC_UA,
                        mb,
                        i);
                sfeeController.init(comConfig);

                sfeeControllers.add(sfeeController);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sfemMonitor = new SFEM_monitor(sfem);
    }

    private modbus searchForOpenConnections(String[] comParams) {
        modbus mb = new modbus();

        for (SFEE_controller sfeeController : sfeeControllers) {
            if (sfeeController.getMb().getIp().equals(comParams[0]))
                if (sfeeController.getMb().getPort() == Integer.parseInt(comParams[1])) {
                    mb = sfeeController.getMb();
                    System.out.println("MB found! " + mb);
                    break;
                }
        }

        return mb;
    }

    public void firstRun(boolean run) {
        if (run)
            for (SFEE_controller sfeeController : sfeeControllers) {
                sfeeController.launchSetup();
            }
        else {

            sfem.getSFEEbyIndex(0).getSFEIbyIndex(0).setMinOperationTime(9);
            sfem.getSFEEbyIndex(0).getSFEIbyIndex(1).setMinOperationTime(33);
            sfem.getSFEEbyIndex(0).getSFEIbyIndex(2).setMinOperationTime(8);

            sfem.getSFEEbyIndex(1).getSFEIbyIndex(0).setMinOperationTime(9);
            sfem.getSFEEbyIndex(1).getSFEIbyIndex(1).setMinOperationTime(33);
            sfem.getSFEEbyIndex(1).getSFEIbyIndex(2).setMinOperationTime(8);
        }

    }

    public void setupFailureMode() {

        for (SFEE_controller sfeeController : sfeeControllers) {
            sfeeController.initFailures();
        }
    }

    private boolean firstExe = true;
    private List<Long> runtime = new ArrayList<>();

    @Override
    public void run() {
        Instant start_t = Instant.now();
        if (firstExe) {
            System.out.print("Press ENTER to start simulation");
            Scanner in = new Scanner(System.in);
            in.nextLine();

            for (SFEE_controller sfeeController : sfeeControllers) {
                sfeeController.getMb().reOpenConnection();
            }

            // Launch simulation
            sfeeControllers.get(0).launchSimulation();
            firstExe = false;
        }

//        Instant sfeeControllers_t = Instant.now();
        for (SFEE_controller sfeeController : sfeeControllers) {
            sfeeController.loop();
        }
//        System.out.println("SFEE Controllers time (ms) " + Duration.between(sfeeControllers_t, Instant.now()).toMillis());

//        Instant sfemMonitor_t = Instant.now();
        sfemMonitor.loop(runtime);
//        System.out.println("SFEM Monitor time (ms) " + Duration.between(sfemMonitor_t, Instant.now()).toMillis());
        runtime.add(Duration.between(start_t, Instant.now()).toMillis());
        //System.out.println("Cycle duration (ms): " + Duration.between(start_t, Instant.now()).toMillis());
    }

    public void start() {

/*        System.out.print("Press ENTER to start simulation");
        Scanner in = new Scanner(System.in);
        in.nextLine();

        for (Map.Entry<Integer, SFEE> entry : sfem.getSFEEs().entrySet()) {
            entry.getValue().getMb().reOpenConnection();
        }

        for (SFEE_controller sfeeController : sfeeControllers) {
            sfeeController.startMonitoring();
        }

        for (SFEE_controller sfeeController : sfeeControllers) {
            sfeeController.startFailures();
        }


        // Launch monitor thread
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(sfemMonitor, 0, 100, TimeUnit.MILLISECONDS);

        // Launch simulation
        sfeeControllers.get(0).launchSimulation();*/

    }
}
