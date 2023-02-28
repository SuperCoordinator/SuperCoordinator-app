import controllers.SFEM_controller;
import models.SFEE;
import models.SFEM;
import monitor.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App2 {

    public static void main(String[] args) {


        ArrayList<SFEM> SFEMs = new ArrayList<>();
        SFEM newSFEM = new SFEM("SFEM_test");

        SFEM_controller sfemController = new SFEM_controller(newSFEM);
        sfemController.init();

        sfemController.firstRun(false);
        sfemController.setupFailureMode();

        sfemController.start();
        while (true) {
            System.out.println("Number of running Threads: " + Thread.activeCount());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        }


       /* currSFEE.addNewSFEI_conveyor(
                "entry_conveyor",
                "s_emitter",
                "s_lids_at_entry",
                Instant.now(),
                Instant.now(),
                "entry_remover",
                "entry_emitter",
                "s_entry_remover",
                "s_entry_emitter");
        currSFEE.addNewSFEI_machine(
                "MC1",
                "s_lids_at_entry",
                "s_lids_at_exit",
                Instant.now(),
                Instant.now(),
                "MC1_stop");
        currSFEE.addNewSFEI_conveyor(
                "exit_conveyor",
                "s_lids_at_exit",
                "s_remover",
                Instant.now(),
                Instant.now(),
                "exit_remover",
                "exit_emitter",
                "s_exit_remover",
                "s_exit_emitter");*/

/*        // Write the Serial layout ( C - conv / M - machine / W -warehouse
        String layoutSketch = "CmC";
        layoutSketch = layoutSketch.toLowerCase();
        for (int i = 0; i < layoutSketch.length(); i++) {
            if (String.valueOf(layoutSketch.charAt(i)).equals("c")) {
                if (currSFEE.getSFEE_type().equals(SFEE.SFEE_type.SIMULATION))
                    currSFEE.addNewSFEI_conveyor(
                            "entry_conveyor",
                            "s_emitter",
                            "s_lids_at_entry",
                            Instant.now(),
                            Instant.now(),
                            "entry_remover",
                            "entry_emitter",
                            "s_entry_remover",
                            "s_entry_emitter");
                else if (currSFEE.getSFEE_type().equals(SFEE.SFEE_type.REAL)) {
                    currSFEE.addNewSFEI_conveyor(
                            "entry_conveyor",
                            "s_emitter",
                            "s_lids_at_entry",
                            Instant.now(),
                            Instant.now(),
                            "entry_conveyor");
                }

            } else if (String.valueOf(layoutSketch.charAt(i)).equals("m")) {
                currSFEE.addNewSFEI_machine(
                        "MC1",
                        "s_lids_at",
                        "s_lids_at_entry",
                        Instant.now(),
                        Instant.now(),
                        "entry_conveyor");
            }
        }*/

        /* For simplification, the values are wrote already
         // entryConv - 9s
         // MC1 - 33 s
         // exitConv - 8s
        currSFEE.launchSetup();
         */

      /*  currSFEE.getSFEIbyIndex(0).setMinOperationTime(9);
        currSFEE.getSFEIbyIndex(1).setMinOperationTime(33);
        currSFEE.getSFEIbyIndex(2).setMinOperationTime(8);

        System.out.print("Press ENTER to start simulation");
        Scanner in = new Scanner(System.in);
        in.nextLine();

        currSFEE.openCommunication("192.168.240.1", 502, 1);
        currSFEE.getMb().writeState(currSFEE.getIObyName("FACTORY I/O (Run)"), "1");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(currSFEM.getSFEEs().size());
        ArrayList<SFEE_monitor> SFEEs_monitor = new ArrayList<>();
        for (Map.Entry<Integer, SFEE> sfee : currSFEM.getSFEEs().entrySet()) {
            SFEEs_monitor.add(SFEEs_monitor.size(), new SFEE_monitor(sfee.getValue()));
            scheduler.scheduleAtFixedRate(SFEEs_monitor.get(SFEEs_monitor.size() - 1), 0, 50, TimeUnit.MILLISECONDS);
        }

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new SFEM_monitor(currSFEM,SFEEs_monitor), 0, 100, TimeUnit.MILLISECONDS);*/

        // <------------------------

        //exit(0);


        /*String in;
        ArrayList<SFEM> sfems = new ArrayList<>();
        int iCurrBlock = -1;

        Scanner input = new Scanner(System.in);

        int state = 0;

        while (state != -1) {
            try {
                switch (state) {
                    case 0 -> {
                        // Clean memory
                        if (iCurrBlock != -1)
                            sfems.clear();

                        System.out.println("*********************************************");
                        System.out.println("**** SuperCoordinator Terminal Interface ****");
                        System.out.println("******** STEP 1 - Configuration Phase *******");
                        System.out.println("*********************************************");
                        System.out.println();
                        System.out.println("    1 - Create new SFEM; ");
                        System.out.println("    0 - Exit program;");
                        in = input.nextLine();

                        if (Integer.parseInt(in) == 1)
                            state = 1;
                        else if (Integer.parseInt(in) == 0) {
                            state = -1;
                        }

                    }
                    case 1 -> {
                        System.out.println("**** New Shop Floor Education Module (SFEM) ****");
                        System.out.println();
                        System.out.print(" SFEM name: ");
                        String name = input.nextLine();

                        System.out.println("**** Communication Protocol ****");
                        System.out.println();
                        System.out.println("    1 - Modbus TCP/IP; ");
                        //System.out.println("    2 - OPC UA;");
                        System.out.println("    0 - Go back;");
                        in = input.nextLine();
                        if (Integer.parseInt(in) == 1)
                            state = 2;
                        else if (Integer.parseInt(in) == 0) {
                            state = 0;
                            break;
                        }

                        sfems.add(new SFEM(name));
                        iCurrBlock = sfems.size() - 1;
                    }
                    case 2 -> {
                        System.out.println("**** Communication Protocol -> Modbus ****");
                        System.out.println();
                        System.out.print("    IP: ");
                        String ip = input.nextLine();

                        System.out.print("    Port: ");
                        int port = Integer.parseInt(input.nextLine());
                        System.out.print("    Slave ID (0-255): ");
                        int slaveID = Integer.parseInt(input.nextLine());

                        sfems.get(iCurrBlock).openCommunication(ip, port, slaveID);

                        System.out.println();
                        System.out.println("Connection Established!");
                        state = 3;
                    }
                    case 3 -> {
                        System.out.println("**** Define IO mapping ****");
                        System.out.println();
                        System.out.println("    1 - Import from CSV file; ");
                        System.out.println("    0 - Go back;");
                        //System.out.println("    2 - Add manually;");
                        in = input.nextLine();

                        if (Integer.parseInt(in) == 1) {
                            System.out.print("    File location (path):");
                            String io_file_path = input.nextLine();
                            sfems.get(iCurrBlock).importIO(io_file_path);
                            ++state;
                        } else if (Integer.parseInt(in) == 0) {
                            state = 0;
                        }

                        System.out.print("    Print imported IO (y/n)? ");
                        String ans = input.nextLine();
                        if (ans.contains("y"))
                            sfems.get(iCurrBlock).printAllIO();

                    }
                    default -> {
                    }
                }



            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Exit program
        for (SFEM block : sfems)
            block.closeCommunication();

*/
    }
}
