package controllers;

import communication.modbus;
import models.base.SFEE;
import models.SFEx_particular.SFEM_production;
import monitor.SFEM_monitor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SFEM_production_controller implements Runnable {

    private final SFEM_production sfem;

    private SFEM_monitor sfemMonitor;

    ArrayList<SFEE_controller> sfeeControllers;
    private final viewers.SFEM viewer;

    public SFEM_production_controller(SFEM_production sfem) {
        this.sfem = sfem;

        this.sfeeControllers = new ArrayList<>();
        this.viewer = new viewers.SFEM();

    }

    public void init_SFEEs() {

        try {
            // # of SFEE to be added
            //String input = viewer.nSFEE();
            String input = "2";
            for (int i = 0; i < Integer.parseInt(input); i++) {

                //String[] inputs = viewer.SFEE_params(i);
                String[] inputs = {"sfee" + (i + 1), "1", "1"};

                SFEE sfee = new SFEE(
                        inputs[0],
                        Integer.parseInt(inputs[2]) == 1 ? SFEE.SFEE_type.SIMULATION : SFEE.SFEE_type.REAL,
                        Integer.parseInt(inputs[1]) == 1 ? SFEE.communicationOption.MODBUS : SFEE.communicationOption.OPC_UA);

                sfem.addNewSFEE(sfee);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        sfemMonitor = new SFEM_monitor(sfem);
    }

    public void init_SFEE_controllers() {
        int i = 0;
        for (Map.Entry<Integer, SFEE> sfee : sfem.getSFEEs().entrySet()) {
            /* QUESTAO DO SLAVE ID*/
            String[] comConfig = viewer.communicationParams(0);

            modbus mb = new modbus(comConfig[0], Integer.parseInt(comConfig[1]), Integer.parseInt(comConfig[2]));
            SFEE_controller sfeeController = new SFEE_controller(
                    sfee.getValue(),
                    mb,
                    i);
            sfeeController.init(comConfig);

            firstRun(false, i);

            sfeeController.initFailures();

            sfeeControllers.add(sfeeController);
            i++;
        }


    }

    public void openConnections() {

        // Open the first connection
        sfeeControllers.get(0).openCommunication();
        System.out.println(" SFEE (" + 0 + ") mb:" + sfeeControllers.get(0).getMb());
        // For the rest, first check if there are common connections
        for (int i = 1; i < sfeeControllers.size(); i++) {
            SFEE_controller to_define = sfeeControllers.get(i);
            System.out.println(" SFEE (" + i + ") mb:" + to_define.getMb());
            modbus found_mb = null;

            for (int j = 0; j < sfeeControllers.size(); j++) {
                SFEE_controller temp = sfeeControllers.get(j);
                if (j == i)
                    continue;
                if (!to_define.getMb().isConfigured() && temp.getMb().isConfigured()) {
                    if (to_define.getMb().getIp().equals(temp.getMb().getIp()))
                        if (to_define.getMb().getPort() == temp.getMb().getPort()) {
                            found_mb = temp.getMb();
                            System.out.println(" FOUND for SFEE (" + j + ") mb:" + temp.getMb());
                            break;
                        }
                }
            }

            if (found_mb != null) {
                to_define.setMb(found_mb);
            } else {
                to_define.openCommunication();
            }
        }

    }

    private modbus searchForCommonConnections(String[] comParams) {
        modbus mb = null;

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

    public void firstRun(boolean run, int itr) {
        if (run)
            for (SFEE_controller sfeeController : sfeeControllers) {
                sfeeController.launchSetup();
            }
        else {

            sfem.getSFEEbyIndex(itr).getSFEIbyIndex(0).setMinOperationTime(9);
            sfem.getSFEEbyIndex(itr).getSFEIbyIndex(1).setMinOperationTime(33);
            sfem.getSFEEbyIndex(itr).getSFEIbyIndex(2).setMinOperationTime(8);

/*            sfem.getSFEEbyIndex(1).getSFEIbyIndex(0).setMinOperationTime(9);
            sfem.getSFEEbyIndex(1).getSFEIbyIndex(1).setMinOperationTime(33);
            sfem.getSFEEbyIndex(1).getSFEIbyIndex(2).setMinOperationTime(8);*/
        }

    }

    public void setupFailureMode() {

        for (SFEE_controller sfeeController : sfeeControllers) {
            sfeeController.initFailures();
        }
    }

    private boolean firstExe = true;
    private final List<Long> runtime = new ArrayList<>();

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

        for (SFEE_controller sfeeController : sfeeControllers) {
            sfeeController.loop();
        }


//        Instant sfemMonitor_t = Instant.now();
        sfemMonitor.loop(runtime);
//        System.out.println("SFEM_production Monitor time (ms) " + Duration.between(sfemMonitor_t, Instant.now()).toMillis());
        runtime.add(Duration.between(start_t, Instant.now()).toMillis());
        //System.out.println("Cycle duration (ms): " + Duration.between(start_t, Instant.now()).toMillis());
    }

}
