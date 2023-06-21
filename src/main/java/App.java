import communication.database.dbConnection;
import controllers.production.cSFEE_production;
import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import controllers.warehouse.cSFEM_warehouse;
import models.sfe_x.SFEM_production;
import models.sfe_x.SFEM_transport;
import models.sfe_x.SFEM_warehouse;
import models.base.SFEE;
import models.base.SFEI;
import org.apache.commons.math3.util.Pair;
import utility.serialize.serializer;
import utility.utils;
import viewers.SFEE_transport;
import viewers.runtimeUI.C_Runtime;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.exit;


public class App {

    public static void main(String[] args) {
        try {

            Instant start_t = Instant.now();
            String input = "0";
            boolean exit = false;

            ExecutorService executor = Executors.newSingleThreadExecutor();

            do {
                switch (input) {
                    case "0" -> {
                        System.out.println("****************************");
                        System.out.println("***** SuperCoordinator *****");
                        System.out.println("****************************");
                        System.out.println();
                        System.out.println("1 - New configuration");
                        System.out.println("2 - Load configuration");
                        input = String.valueOf(utils.getInstance().validateUserOption(1, 2));
                    }

                    case "1" -> {
                        String scene = newConfiguration();
                        runApplication(scene);
                        start_t = Instant.now();
                        System.out.println("# Threads: " + Thread.activeCount());
                        input = "3";
                    }
                    case "2" -> {
                        String scene = loadConfiguration();
                        runApplication(scene);
                        start_t = Instant.now();
                        System.out.println("# Threads: " + Thread.activeCount());
                        input = "3";
                    }
                    case "3" -> {
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
        System.out.println("Closing SuperCoordinator...");
        // Close modbus connection
        for (cSFEM_production production : serializer.getInstance().getC_Production()) {
            production.endSimulation();
        }
        // Save XML with failures records
        System.out.println("Saving Failures History...");
        serializer.getInstance().saveFailuresHistory();

        exit(0);


    }

    private static int liveStats() {
        Scanner in = new Scanner(System.in);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println("   1 - Monitor/Tracking Parts");
        System.out.println("   2 - Failures History      ");
        System.out.println("   3 - Threads list          ");
        System.out.println("   e - Exit execution        ");
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.print("> ");

        String input = in.nextLine();
        if (input.equalsIgnoreCase("e")) {
            System.out.println("Exit detected...");
            return -1;
        }

        int opt = Integer.parseInt(input);
        do {
            if (opt < 1 || opt > 3)
                System.out.println("Invalid option. Try again!");
            System.out.print("> ");
            opt = Integer.parseInt(in.nextLine());
        } while (opt < 1 || opt > 3);


        if (opt == 1) {
            System.out.println("Monitor/Tracking Parts");
            System.out.println("   From SFEM ? ");
            int i = 0;
            for (cSFEM_production cSFEMProduction : serializer.getInstance().getC_Production()) {
                System.out.println("     " + i + " - " + cSFEMProduction.getSfem().getName());
                i++;
            }
            opt = utils.getInstance().validateUserOption(0, serializer.getInstance().getC_Production().size() - 1);

            cSFEM_production production = serializer.getInstance().getC_Production().get(opt);
            i = 0;
            System.out.println("     And which SFEE of " + production.getSfem().getName());
            for (cSFEE_production cSFEEProduction : production.getSfeeControllers()) {
                System.out.println("     " + i + " - " + cSFEEProduction.getSFEE().getName());
                i++;
            }
            opt = utils.getInstance().validateUserOption(0, serializer.getInstance().getC_Production().size() - 1);

            production.getSfeeControllers().get(opt).getSfeeMonitor().monitorSFEE();

        } else if (opt == 2) {
            for (cSFEM_production cSFEMProduction : serializer.getInstance().getC_Production()) {
                for (cSFEE_production cSFEEProduction : cSFEMProduction.getSfeeControllers()) {
                    for (SFEI sfei : cSFEEProduction.getSFEE().getSFEIs().values()) {
                        System.out.println(Arrays.toString(sfei.getFailuresHistory().values().toArray()));
                    }
                }
            }
        } else if (opt == 3) {
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            for (Thread x : threadSet) {
                System.out.println(x.getName());
            }
        }

        return 0;

    }


    private static void runApplication(String scene) {
        // Threads for Production/Transport/Warehouse Modules Controllers, Database and Runtime Interface
        int poolsize = serializer.getInstance().getC_Production().size() + serializer.getInstance().getC_Transport().size() + 3;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolsize);

        /* ---- DATABASE  ---- */
        serializer.getInstance().updateDB(scene);
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
        ArrayList<cSFEE_production> sfees = new ArrayList<>();

        for (cSFEM_production production : serializer.getInstance().getC_Production()) {
            scheduler.scheduleAtFixedRate(production, 0, 100, TimeUnit.MILLISECONDS);
            sfees.addAll(production.getSfeeControllers());
        }

        for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
            scheduler.scheduleAtFixedRate(transport, 0, 100, TimeUnit.MILLISECONDS);
        }

        scheduler.scheduleAtFixedRate(new C_Runtime(sfees), 0, 1, TimeUnit.SECONDS);

    }

    private static String newConfiguration() {
        Scanner in = new Scanner(System.in);

        System.out.println("Configuration name");
        System.out.print("> ");
        String confName = in.nextLine();

        System.out.println("Folder path for saving XML configuration file");
        System.out.print("> ");
        String xmlPath = in.nextLine();

        serializer.getInstance().setFailuresHistoryPath(xmlPath + "/failuresOccurrences/");

        xmlPath = xmlPath + "/" + confName + ".xml";

        System.out.println("Folder path to database files:");
        System.out.print("> ");
        serializer.getInstance().setDatabasePath(new Scanner(System.in).nextLine());

        System.out.println("Folder path to inbound orders");
        System.out.print("> ");
        serializer.getInstance().setInboundOrdersPath(new Scanner(System.in).nextLine());

        /* WAREHOUSE MODULE */
        SFEM_warehouse sfemWarehouse = new SFEM_warehouse(SFEM_warehouse.warehouseOrganization.RANDOM);
        cSFEM_warehouse cSFEMWarehouse = new cSFEM_warehouse(sfemWarehouse, 5);
        cSFEMWarehouse.init();

        serializer.getInstance().setC_Warehouse(cSFEMWarehouse);

        // Create new configuration files
        /* PRODUCTION MODULES*/
        System.out.println("*** Production Modules SETUP ***");
        System.out.println("Number of Modules to configure? ");
        System.out.print("> ");
        int nModules = Integer.parseInt(in.nextLine());

        for (int i = 0; i < nModules; i++) {
            System.out.println("Production Module (SFEM) number " + i + " name ?");
            System.out.print("> ");
            String sfemName = in.nextLine();
            SFEM_production sfemProduction = new SFEM_production(sfemName);

            cSFEM_production sfemController = new cSFEM_production(sfemProduction);
            System.out.println("Number of Production Elements (SFEEs) for " + sfemName + " ?");
            System.out.print("> ");
            sfemController.init_SFEEs(Integer.parseInt(in.nextLine()), sfemName);
            sfemController.init_SFEE_controllers(/*serializer.getInstance().scene.ordinal(), i*/);

            serializer.getInstance().getC_Production().add(i, sfemController);
        }

        // Open communications
        for (cSFEM_production production : serializer.getInstance().getC_Production()) {
            production.openConnections();
        }

        //Intermediate save before Transport Modules setup
        serializer.getInstance().saveXML(xmlPath);

        System.out.println("*** Warehouse/Transport Modules SETUP ***");
        System.out.println("Warehouse Organization");
        for (int i = 0; i < SFEM_warehouse.warehouseOrganization.values().length; i++) {
            System.out.println("    " + i + " - " + SFEM_warehouse.warehouseOrganization.values()[i].name());
        }
        int opt = utils.getInstance().validateUserOption(0, SFEM_warehouse.warehouseOrganization.values().length - 1);
        cSFEMWarehouse.getSfem().setWarehouseOrganization(SFEM_warehouse.warehouseOrganization.values()[opt]);
        System.out.println("Check new orders frequency (and expedition) in minutes");
        System.out.println("> ");
        int check_period = Integer.parseInt(in.nextLine());
        cSFEMWarehouse.setCheckOrders_period(check_period);

        System.out.println("Start of Line Item (SFEI) to connect with the Warehouse is Simulation (y) or Real(n) ?");
        boolean isSimulation = utils.getInstance().validateUserOption();
        if (isSimulation) {
            initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.WH2SFEI);
        } else {
            initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.WH2RealSFEI);
        }

        System.out.println("Number END Items that links with the warehouse ? ");
        System.out.print("> ");
        nModules = Integer.parseInt(in.nextLine());
        for (int i = 0; i < nModules; i++) {
            System.out.println("Defining connection end item " + i + " of " + nModules);
            System.out.println("End of Line Item (SFEI) to connect with the Warehouse is Simulation (y) or Real(n) ?");
            isSimulation = utils.getInstance().validateUserOption();
            if (isSimulation) {
                initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.SFEI2WH);
            } else {
                initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.RealSFEI2WH);
            }
        }

        System.out.println("Number of Transport Modules to configure? ");
        System.out.println("INFO: Transport Modules connects Elements");
        System.out.print("> ");
        nModules = Integer.parseInt(in.nextLine());
        for (int i = 0; i < nModules; i++) {
            initializeTransport(null, SFEM_transport.configuration.SFEI2SFEI);
        }

        serializer.getInstance().saveXML(xmlPath);

        return confName;
    }

    private static void initializeTransport(cSFEM_warehouse cSFEMWarehouse, SFEM_transport.configuration transportConfig) {
        Scanner in = new Scanner(System.in);

        ArrayList<Object> data = new ArrayList<>();
        ArrayList<Object> controllerData = new ArrayList<>();
        ArrayList<Object> operationModeData = new ArrayList<>();

        SFEM_transport.configuration configuration = transportConfig;

        switch (transportConfig) {
            case SFEI2SFEI -> {
                System.out.print("Name Transport Module (SFEM)");
                String sfem_transport_name = in.nextLine();

                System.out.print("Name Transport Element (SFEE)");
                String sfee_transport_name = in.nextLine();

                System.out.println("Input Element (SFEE) name to connect with " + sfem_transport_name);
                Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                if (inSFEE.getFirst().getSfeeEnvironment().equals(SFEE.SFEE_environment.REAL))
                    configuration = SFEM_transport.configuration.RealSFEI2SFEI;

                System.out.println("Connection Item (SFEI)");
                int index = 0;
                for (Map.Entry<Integer, SFEI> entry : inSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI inSFEI = inSFEE.getFirst().getSFEIbyIndex(utils.getInstance().validateUserOption(0, inSFEE.getFirst().getSFEIs().size() - 1));

                System.out.println("Output Element (SFEE) name to connect with " + sfem_transport_name);
                Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                if (outSFEE.getFirst().getSfeeEnvironment().equals(SFEE.SFEE_environment.REAL))
                    configuration = SFEM_transport.configuration.SFEI2RealSFEI;

                System.out.println("Connection Item (SFEI)");
                index = 0;
                for (Map.Entry<Integer, SFEI> entry : outSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI outSFEI = outSFEE.getFirst().getSFEIbyIndex(utils.getInstance().validateUserOption(0, outSFEE.getFirst().getSFEIs().size() - 1));

                SFEE_transport viewer = new SFEE_transport();
                String[] in_SensAct = viewer.associateSensor2Actuator(false, inSFEE.getFirst(), inSFEI.getOutSensor().getName());
                String[] out_SensAct = viewer.associateSensor2Actuator(true, outSFEE.getFirst(), outSFEI.getInSensor().getName());

                controllerData.add(0, "SFEI_transport " + serializer.getInstance().getC_Transport().size());
                controllerData.add(1, inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()));
                controllerData.add(2, outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()));
                controllerData.add(3, inSFEE.getFirst());
                controllerData.add(4, outSFEE.getFirst());
                controllerData.add(5, inSFEI.getName());
                controllerData.add(6, outSFEI.getName());
                controllerData.addAll(List.of(in_SensAct));
                controllerData.addAll(List.of(out_SensAct));

                operationModeData = new ArrayList<>(List.of(viewer.SFEE_stochasticTime(sfee_transport_name)));

                data.add(0, sfem_transport_name);
                data.add(1, sfee_transport_name);

            }
            case WH2SFEI, WH2RealSFEI -> {
                // Special Transport Module: warehouse -> in SFEI

                data.add(0, "SFEM_T_WH2SFEI");
                data.add(1, "SFEE_T_WH2SFEI");

                System.out.println("Enter Element (SFEE) name to connect with the warehouse?");
                Pair<SFEE, cSFEM_production> conSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                System.out.println("Which SFEI?");
                int index = 0;
                for (Map.Entry<Integer, SFEI> entry : conSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI conSFEI = conSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));
                SFEE_transport viewer = new SFEE_transport();

                String[] visionSensorsRelation = viewer.associateSensor2Actuator(true, conSFEE.getFirst(), conSFEI.getInSensor().getName());

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

                operationModeData = new ArrayList<>(List.of(viewer.SFEE_stochasticTime("SFEE_T_WH2SFEI")));
            }
            case SFEI2WH, RealSFEI2WH -> {
                // Special Transport Module: in SFEI -> warehouse

                data.add(0, "SFEM_T_SFEI2WH");
                data.add(1, "SFEE_T_SFEI2WH");

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

                String[] visionSensorsRelation = viewer.associateSensor2Actuator(false, conSFEE.getFirst(), conSFEI.getOutSensor().getName());

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

                operationModeData = new ArrayList<>(List.of(viewer.SFEE_stochasticTime("SFEE_T_SFEI2WH")));

            }
        }


        serializer.getInstance().new_cSFEM_transport(configuration, data, controllerData, operationModeData);

    }

    private static String loadConfiguration() {

        System.out.println("Path to XML file");
        System.out.print("> ");
        // Load existing configuration files
        String filePath = new Scanner(System.in).nextLine();
        String fileName = new File(filePath).getName();
        String scene = fileName.split(".xml")[0];
        // Deserialize Production Controllers
        serializer.getInstance().loadXML(filePath);

        /* ---- DATABASE  ---- */
        System.out.println("EMPTY DATABASE (y/n)?");
        if (utils.getInstance().validateUserOption())
            serializer.getInstance().emptyDB(scene);
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
        return scene;
    }
}

