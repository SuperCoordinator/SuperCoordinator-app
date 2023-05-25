import communication.database.dbConnection;
import controllers.production.cSFEE_production;
import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import controllers.warehouse.cSFEM_warehouse;
import javafx.application.Application;
import models.SFEx.SFEM_production;
import models.SFEx.SFEM_transport;
import models.SFEx.SFEM_warehouse;
import models.base.SFEE;
import models.base.SFEI;
import org.apache.commons.math3.util.Pair;
import utility.serialize.serializer;
import viewers.SFEE_transport;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.exit;
import static java.lang.System.in;


public class App {

    public static void main(String[] args) {
        try {
            Scanner in = new Scanner(System.in);
            Instant start_t = Instant.now();
            String input = "0";
            boolean exit = false;
            do {
                switch (input) {
                    case "0" -> {
                        System.out.println("****************************");
                        System.out.println("***** SuperCoordinator *****");
                        System.out.println("****************************");
                        System.out.println();
                        System.out.println("   1 - New configuration");
                        System.out.println("   2 - Load configuration");
//                        System.out.println("   e - Exit execution");
                        System.out.print("option: ");
                        input = in.nextLine();
                    }

                    case "1" -> {
                        newConfiguration();
                        runApplication();
                        start_t = Instant.now();
                        input = "3";
                    }
                    case "2" -> {
                        loadConfiguration();
                        runApplication();
                        start_t = Instant.now();
                        input = "3";
                    }
                    case "3" -> {
                        ExecutorService executor = Executors.newCachedThreadPool();
                        Callable<Object> task = App::liveStats;
                        Future<Object> future = executor.submit(task);
                        try {
                            Object result = future.get(60, TimeUnit.SECONDS);
                            if ((int) result == -1)
                                exit = true;
                        } catch (TimeoutException ex) {
                            // handle the timeout
                        } catch (InterruptedException e) {
                            // handle the interrupts
                        } catch (ExecutionException e) {
                            // handle other exceptions
                        } finally {
                            future.cancel(true); // may or may not desire this
                        }
                    }
                }

            } while (!exit && !(Duration.between(start_t, Instant.now()).toMinutes() >= 119));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Stop simulation

        // Save XML with failures records
        serializer.getInstance().saveFailuresHistory();

        exit(0);
    }

    private static int liveStats() {
        Scanner in = new Scanner(System.in);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println("   1 - Monitor/Tracking Parts");
        System.out.println("   2 - Failures History      ");
        System.out.println("   e - Exit execution        ");
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.print("option: ");
        // Se encrava aqui como dar o timeout por tempo???
        String input = in.nextLine();
        if (input.equalsIgnoreCase("e"))
            return -1;

        int opt = 1;
        do {
            if (opt < 1 || opt > 2)
                System.out.println("Invalid option! Try again");
            opt = Integer.parseInt(input);
        } while (opt < 1 || opt > 2);

        if (opt == 1) {
            System.out.println("Monitor/Tracking Parts");
            System.out.println("   From SFEM ? ");
            int i = 1;
            for (cSFEM_production cSFEMProduction : serializer.getInstance().getC_Production()) {
                System.out.println("     " + i + " - " + cSFEMProduction.getSfem().getName());
                i++;
            }
            do {
                if (opt < 1 || opt > serializer.getInstance().getC_Production().size())
                    System.out.println("Invalid option! Try again");
                opt = Integer.parseInt(in.nextLine());
            } while (opt < 1 || opt > serializer.getInstance().getC_Production().size());
            cSFEM_production production = serializer.getInstance().getC_Production().get(opt);
            i = 1;
            System.out.println("     And which SFEE of " + production.getSfem().getName());
            for (cSFEE_production cSFEEProduction : production.getSfeeControllers()) {
                System.out.println("     " + i + " - " + cSFEEProduction.getSFEE().getName());
                i++;
            }
            do {
                if (opt < 1 || opt > serializer.getInstance().getC_Production().size())
                    System.out.println("Invalid option! Try again");
                opt = Integer.parseInt(in.nextLine());
            } while (opt < 1 || opt > serializer.getInstance().getC_Production().size());

            production.getSfeeControllers().get(opt).getSfeeMonitor().monitorSFEE();

        } else if (opt == 2) {

        }

        return 0;

    }


    private static void runApplication() {

        int poolsize = serializer.getInstance().getC_Production().size() + serializer.getInstance().getC_Transport().size() + 2;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolsize);

        /* ---- DATABASE  ---- */
        serializer.getInstance().updateDB();
//            dbConnection.getInstance().initializeDB();
        scheduler.scheduleAtFixedRate(dbConnection.getInstance(), 0, 1, TimeUnit.SECONDS);
        /* ------------------- */

        System.out.print("Press ENTER to start simulation");
        new Scanner(System.in).nextLine();
        System.out.println();
        // Function for start all simulations
        // 1 SFEM -> having 1SFEE or nSFEEs, is the same because the modbus connection is only 1 per simulation
        for (cSFEM_production production : serializer.getInstance().getC_Production()) {
            production.startSimulation();
        }

        scheduler.scheduleAtFixedRate(serializer.getInstance().getC_Warehouse(), 0, 1, TimeUnit.SECONDS);

        for (cSFEM_production production : serializer.getInstance().getC_Production()) {
            scheduler.scheduleAtFixedRate(production, 0, 100, TimeUnit.MILLISECONDS);
        }

        for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
            scheduler.scheduleAtFixedRate(transport, 0, 100, TimeUnit.MILLISECONDS);
        }
    }

    private static void newConfiguration() {
        Scanner in = new Scanner(System.in);

        System.out.print("Path to save XML configuration file: ");
        String xmlPath = in.nextLine();

        System.out.print("Path to save future Failures Occurrences XML file: ");
        serializer.getInstance().setFailuresHistoryPath(new Scanner(System.in).nextLine());

        /* WAREHOUSE MODULE */
        SFEM_warehouse sfemWarehouse = new SFEM_warehouse(SFEM_warehouse.warehouseOrganization.RANDOM);
        cSFEM_warehouse cSFEMWarehouse = new cSFEM_warehouse(sfemWarehouse, 5);
        cSFEMWarehouse.init();

        serializer.getInstance().setC_Warehouse(cSFEMWarehouse);

        // Create new configuration files
        /* PRODUCTION MODULES*/
        int nModules = 1;

        if (serializer.getInstance().scene.equals(serializer.scenes.CMC_connection)) {
            nModules = 1;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.CMC2_con_individual)) {
            nModules = 2;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.sorting_station)) {
            nModules = 1;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS_3CMC)) {
            nModules = 2;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.MC_Staudinger)) {
            nModules = 1;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS_WH)) {
            nModules = 1;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS_3CMC_WH)) {
            nModules = 1;
        }

        for (int i = 0; i < nModules; i++) {
            System.out.println("SFEM Production " + i + " name ?");
            SFEM_production sfemProduction = new SFEM_production(in.nextLine());

            cSFEM_production sfemController = new cSFEM_production(sfemProduction);
            System.out.println("How many SFEEs Production for " + sfemProduction.getName() + " ?");
            sfemController.init_SFEEs(Integer.parseInt(in.nextLine()));
            sfemController.init_SFEE_controllers(serializer.getInstance().scene.ordinal(), i);

            serializer.getInstance().getC_Production().add(i, sfemController);
        }

        // Open communications
        for (cSFEM_production production : serializer.getInstance().getC_Production()) {
            production.openConnections();

        }

        System.out.print("Path to save configuration XML file: ");
        //Intermediate save before Transport Modules setup
        serializer.getInstance().saveXML(xmlPath);

        /* TRANSPORT MODULES*/
/*          System.out.println("How many Transport SFEModules ?");
            String str = in.nextLine();
            int nModules = Integer.parseInt(str);*/
        if (serializer.getInstance().scene.equals(serializer.scenes.sorting_station))
            nModules = 0;
        else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS_3CMC)) {
            nModules = 3;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.MC_Staudinger)) {
            nModules = 0;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS_WH)) {
            nModules = 0;
        } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS_3CMC_WH)) {
            nModules = 3;
        } else
            nModules = 1;

        if (!serializer.getInstance().scene.equals(serializer.scenes.MC_Staudinger)) {
            initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.WH2SFEI);
            initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.SFEI2WH);
        } else {
            initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.WH2RealSFEI);
            initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.RealSFEI2WH);
        }


        for (int i = 0; i < nModules; i++) {
            initializeTransport(null, SFEM_transport.configuration.SFEI2SFEI);
        }

        serializer.getInstance().saveXML(xmlPath);
    }

    private static void initializeTransport(cSFEM_warehouse cSFEMWarehouse, SFEM_transport.configuration transportConfig) {
        Scanner in = new Scanner(System.in);

        ArrayList<Object> data = new ArrayList<>();
        ArrayList<Object> controllerData = new ArrayList<>();
        ArrayList<Object> operationModeData = new ArrayList<>();

        SFEM_transport.configuration configuration = transportConfig;

        switch (transportConfig) {
            case SFEI2SFEI -> {
                System.out.print("SFEM transport name ? ");
                String sfem_transport_name = in.nextLine();

                System.out.print("SFEE transport name ? ");
                String sfee_transport_name = in.nextLine();

                System.out.println("Input SFEE name to connect with " + sfem_transport_name + " ?");
                Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                if (inSFEE.getFirst().getSFEE_environment().equals(SFEE.SFEE_environment.REAL))
                    configuration = SFEM_transport.configuration.RealSFEI2SFEI;

                System.out.println("Connection Input SFEI?");
                int index = 0;
                for (Map.Entry<Integer, SFEI> entry : inSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI inSFEI = inSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));

                System.out.println("Output SFEE name to connect with " + sfem_transport_name + " ?");
                Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                if (outSFEE.getFirst().getSFEE_environment().equals(SFEE.SFEE_environment.REAL))
                    configuration = SFEM_transport.configuration.SFEI2RealSFEI;

                System.out.println("Connection Output SFEI?");
                index = 0;
                for (Map.Entry<Integer, SFEI> entry : outSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI outSFEI = outSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));

                SFEE_transport viewer = new SFEE_transport();
                String[] in_SensAct = viewer.associateSensor2Actuator(1, inSFEI.getOutSensor().getName());
                String[] out_SensAct = viewer.associateSensor2Actuator(3, outSFEI.getInSensor().getName());

                controllerData.add(0, "SFEI_transport " + serializer.getInstance().getC_Transport().size());
                controllerData.add(1, inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()));
                controllerData.add(2, outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()));
                controllerData.add(3, inSFEE.getFirst());
                controllerData.add(4, outSFEE.getFirst());
                controllerData.add(5, inSFEI.getName());
                controllerData.add(6, outSFEI.getName());
                controllerData.addAll(List.of(in_SensAct));
                controllerData.addAll(List.of(out_SensAct));

                operationModeData = new ArrayList<>(List.of(viewer.SFEE_stochasticTime()));

                data.add(0, sfem_transport_name);
                data.add(1, sfee_transport_name);

            }
            case WH2SFEI, WH2RealSFEI -> {
                // Special Transport Module: warehouse -> in SFEI

                data.add(0, "SFEM_T_WH2SFEI");
                data.add(1, "SFEE_T_WH2SFEI");

                System.out.println("SFEE name to connect with SFEM_T_WH2SFEI ?");
                Pair<SFEE, cSFEM_production> conSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                System.out.println("Select its SFEI?");
                int index = 0;
                for (Map.Entry<Integer, SFEI> entry : conSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI conSFEI = conSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));
                SFEE_transport viewer = new SFEE_transport();

                String[] visionSensorsRelation = viewer.associateSensor2Actuator(3, conSFEI.getInSensor().getName());

                controllerData.add(0, "SFEI_T_WH2SFEI");
                // MB connection
                controllerData.add(1, null);
                controllerData.add(2, conSFEE.getSecond().searchMBbySFEE(conSFEE.getFirst().getName()));
                // in/out SFEE
                controllerData.add(3, cSFEMWarehouse.getSfem().getSfeeWarehouse());
                controllerData.add(4, conSFEE.getFirst());
                // in/out SFEI
                controllerData.add(5, cSFEMWarehouse.getSfem().getSfeeWarehouse().getSFEIbyIndex(0));
                controllerData.add(6, conSFEI.getName());

                controllerData.add("none");
                controllerData.addAll(List.of(visionSensorsRelation));

                operationModeData = new ArrayList<>(List.of(viewer.SFEE_stochasticTime()));
            }
            case SFEI2WH, RealSFEI2WH -> {
                // Special Transport Module: in SFEI -> warehouse

                data.add(0, "SFEM_T_SFEM2WH");
                data.add(1, "SFEE_T_SFEE2WH");

                System.out.println("SFEE name to connect with SFEM_T_SFEI2WH ?");
                Pair<SFEE, cSFEM_production> conSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                System.out.println("Select its SFEI?");
                int index = 0;
                for (Map.Entry<Integer, SFEI> entry : conSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI conSFEI = conSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));
                SFEE_transport viewer = new SFEE_transport();

                String[] visionSensorsRelation = viewer.associateSensor2Actuator(1, conSFEI.getOutSensor().getName());

                controllerData.add(0, "SFEI_T_SFEI2WH");
                // MB connection
                controllerData.add(1, conSFEE.getSecond().searchMBbySFEE(conSFEE.getFirst().getName()));
                controllerData.add(2, null);
                // in/out SFEE
                controllerData.add(3, conSFEE.getFirst());
                controllerData.add(4, cSFEMWarehouse.getSfem().getSfeeWarehouse());
                // in/out SFEI
                controllerData.add(5, conSFEI.getName());
                controllerData.add(6, cSFEMWarehouse.getSfem().getSfeeWarehouse().getSFEIbyIndex(1));


                controllerData.addAll(List.of(visionSensorsRelation));
                controllerData.addAll(Arrays.asList("none", "none", "none"));

                operationModeData = new ArrayList<>(List.of(viewer.SFEE_stochasticTime()));

            }
        }


        serializer.getInstance().new_cSFEM_transport(configuration, data, controllerData, operationModeData);

    }

    private static void loadConfiguration() {

        System.out.print("Path to XML file: ");
        // Load existing configuration files
        // Deserialize Production Controllers
        serializer.getInstance().loadXML(new Scanner(System.in).nextLine());

        /* ---- DATABASE  ---- */
        System.out.print("EMPTY DATABASE (y/n)? ");
        String input = " ";
        do {
            if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n"))
                System.out.println("Invalid option! Try again");
            input = new Scanner(System.in).nextLine();
        } while (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n"));

        if (input.equalsIgnoreCase("y"))
            serializer.getInstance().emptyDB();
        /* ------------------- */

        // Load Warehouse
        serializer.getInstance().getC_Warehouse().init_afterLoad();

        for (cSFEM_production production : serializer.getInstance().getC_Production()) {
            production.init_after_XML_loading();
        }

        // Open communications
        for (cSFEM_production production : serializer.getInstance().getC_Production()) {
            production.openConnections();

        }

        // Deserialize Transport Controllers
        // Initialize the monitors and controllers with the SFEE objects
        for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
            transport.init_after_XML_load();
        }

        // Set up the connections between SFEEs
        for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
            Pair<Pair<String, String>, Pair<String, String>> names = transport.getPrevNext_SFEE_SFEI_names();

            switch (transport.getSfem().getTransport_configuration()) {
                case SFEI2SFEI, SFEI2RealSFEI, RealSFEI2SFEI -> {
                    Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(names.getFirst().getFirst());
                    Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(names.getSecond().getFirst());

                    transport.setupSFEETransportController(
                            inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()),
                            outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()),
                            serializer.getInstance().searchSFEIbySFEE(inSFEE.getFirst(), names.getFirst().getSecond()),
                            serializer.getInstance().searchSFEIbySFEE(outSFEE.getFirst(), names.getSecond().getSecond()));
                }
                case WH2SFEI, WH2RealSFEI -> {
                    Pair<SFEE, cSFEM_production> conSFEE = serializer.getInstance().searchSFEEbyName(names.getSecond().getFirst());
                    transport.setupSFEETransportController(
                            null,
                            conSFEE.getSecond().searchMBbySFEE(conSFEE.getFirst().getName()),
                            serializer.getInstance().getC_Warehouse().getSfeeWarehouseController().getSfee().getSFEIbyIndex(0),
                            serializer.getInstance().searchSFEIbySFEE(conSFEE.getFirst(), names.getSecond().getSecond()));
                }
                case SFEI2WH, RealSFEI2WH -> {
                    Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(names.getFirst().getFirst());
                    transport.setupSFEETransportController(
                            inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()),
                            null,
                            serializer.getInstance().searchSFEIbySFEE(inSFEE.getFirst(), names.getFirst().getSecond()),
                            serializer.getInstance().getC_Warehouse().getSfeeWarehouseController().getSfee().getSFEIbyIndex(1));
                }
            }
        }
//                serializer.getInstance().saveXML();
    }
}

