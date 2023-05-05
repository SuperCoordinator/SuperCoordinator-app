import communication.database.dbConnection;
import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import controllers.warehouse.cSFEM_warehouse;
import models.SFEx.SFEM_production;
import models.SFEx.SFEM_transport;
import models.SFEx.SFEM_warehouse;
import models.base.SFEE;
import models.base.SFEI;
import org.apache.commons.math3.util.Pair;
import utility.serialize.serializer;
import viewers.SFEE_transport;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class App {


    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        boolean newConfig = false;
        try {
            if (newConfig) {

                /* WAREHOUSE MODULE */
                SFEM_warehouse sfemWarehouse = new SFEM_warehouse();
                cSFEM_warehouse cSFEMWarehouse = new cSFEM_warehouse(sfemWarehouse, 5);
                cSFEMWarehouse.init();

                serializer.getInstance().setC_Warehouse(cSFEMWarehouse);

                // Create new configuration files
                /* PRODUCTION MODULES*/
                int nModules = 1, nSFEE = 1;

                if (serializer.getInstance().scene.equals(serializer.scenes.CMC_connection)) {
                    nModules = 1;
                    nSFEE = 2;
                } else if (serializer.getInstance().scene.equals(serializer.scenes.CMC2_con_individual)) {
                    nModules = 2;
                    nSFEE = 1;
                } else if (serializer.getInstance().scene.equals(serializer.scenes.sorting_station)) {
                    nModules = 1;
                    nSFEE = 3;
                } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS_3CMC)) {
                    nModules = 2;
                    nSFEE = 3;
                } else if (serializer.getInstance().scene.equals(serializer.scenes.MC_Staudinger)) {
                    nModules = 1;
                    nSFEE = 1;
                } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS_WH)) {
                    nModules = 1;
                    nSFEE = 1;
                }

                for (int i = 0; i < nModules; i++) {
                    SFEM_production sfemProduction = new SFEM_production("SFEM_Prod#" + i);

                    cSFEM_production sfemController = new cSFEM_production(sfemProduction);
                    System.out.println("How many SFEE for this Module " + i + "?");
                    sfemController.init_SFEEs(Integer.parseInt(in.nextLine()));
                    sfemController.init_SFEE_controllers(serializer.getInstance().scene.ordinal(), i);

                    serializer.getInstance().getC_Production().add(i, sfemController);
                }

                // Open communications
                for (cSFEM_production production : serializer.getInstance().getC_Production()) {
                    production.openConnections();

                }

/*                serializer.getInstance().loadXML_prod();
                // Open communications
                for (cSFEM_production production : serializer.getInstance().getProduction().getC_Production()) {
                    production.init_after_XML_loading();
                }*/

                //Intermediate save before Transport Modules setup
                serializer.getInstance().saveXML();

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
                } else
                    nModules = 1;

                initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.WH2SFEI);
                initializeTransport(cSFEMWarehouse, SFEM_transport.configuration.SFEI2WH);


                for (int i = 0; i < nModules; i++) {
/*                    SFEM_transport sfemTransport = new SFEM_transport("SFEM_Trans#" + i);
                    cSFEM_transport sfemController = new cSFEM_transport(sfemTransport);

                    sfemController.initSFEE_transport_FromTerminal();*/
                    initializeTransport(null, SFEM_transport.configuration.SFEI2SFEI);

                }
                serializer.getInstance().saveXML();

//                exit(0);

            } else {
                // Load existing configuration files
                // Deserialize Production Controllers
                serializer.getInstance().loadXML();
//                serializer.getInstance().loadXML_prod();

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
//                serializer.getInstance().loadXML_trans();
                // Initialize the monitors and controllers with the SFEE objects
                for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
                    transport.init_after_XML_load();
                }

                // Set up the connections between SFEEs
                for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
                    Pair<Pair<String, String>, Pair<String, String>> names = transport.getPrevNext_SFEE_SFEI_names();

                    switch (transport.getSfem().getTransport_configuration()) {
                        case SFEI2SFEI -> {
                            Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(names.getFirst().getFirst());
                            Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(names.getSecond().getFirst());

                            transport.setupSFEETransportController(
                                    inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()),
                                    outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()),
                                    serializer.getInstance().searchSFEIbySFEE(inSFEE.getFirst(), names.getFirst().getSecond()),
                                    serializer.getInstance().searchSFEIbySFEE(outSFEE.getFirst(), names.getSecond().getSecond()));
                        }
                        case WH2SFEI -> {
                            Pair<SFEE, cSFEM_production> conSFEE = serializer.getInstance().searchSFEEbyName(names.getSecond().getFirst());
                            transport.setupSFEETransportController(
                                    null,
                                    conSFEE.getSecond().searchMBbySFEE(conSFEE.getFirst().getName()),
                                    serializer.getInstance().getC_Warehouse().getSfeeWarehouseController().getSfee().getSFEIbyIndex(0),
                                    serializer.getInstance().searchSFEIbySFEE(conSFEE.getFirst(), names.getSecond().getSecond()));
                        }
                        case SFEI2WH -> {
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

            int poolsize = serializer.getInstance().getC_Production().size() + serializer.getInstance().getC_Transport().size() + 2;
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolsize);

            /* ---- DATABASE  ---- */
            scheduler.scheduleAtFixedRate(dbConnection.getInstance(), 0, 1, TimeUnit.SECONDS);

            serializer.getInstance().updateDB();

            /* ------------------- */

            System.out.print("Press ENTER to start simulation");
            in.nextLine();
            // Function for start all simulations
            // 1 SFEM -> having 1SFEE or nSFEEs, is the same because the modbus connection is only 1 per simulation
            for (cSFEM_production production : serializer.getInstance().getC_Production()) {
                production.startSimulation();
            }

//        exit(0);

            scheduler.scheduleAtFixedRate(serializer.getInstance().getC_Warehouse(), 0, 1, TimeUnit.SECONDS);

            for (cSFEM_production production : serializer.getInstance().getC_Production()) {
                scheduler.scheduleAtFixedRate(production, 0, 100, TimeUnit.MILLISECONDS);
            }

            for (cSFEM_transport transport : serializer.getInstance().getC_Transport()) {
                scheduler.scheduleAtFixedRate(transport, 0, 100, TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private static void initializeTransport(cSFEM_warehouse cSFEMWarehouse, SFEM_transport.configuration transportConfig) {
        Scanner in = new Scanner(System.in);

        ArrayList<Object> data = new ArrayList<>();
        ArrayList<Object> controllerData = new ArrayList<>();
        ArrayList<Object> operationModeData = new ArrayList<>();

        switch (transportConfig) {
            case SFEI2SFEI -> {
                System.out.print("SFEM transport name ? ");
                String sfem_transport_name = in.nextLine();

                System.out.print("SFEE transport name ? ");
                String sfee_transport_name = in.nextLine();

                System.out.println("Output SFEE name to connect with " + sfem_transport_name + " ?");
                Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                System.out.println("Connection Input SFEI?");
                int index = 0;
                for (Map.Entry<Integer, SFEI> entry : inSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI inSFEI = inSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));

                System.out.println("Output SFEE name to connect with " + sfem_transport_name + " ?");
                Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
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
            case WH2SFEI -> {
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
            case SFEI2WH -> {
                // Special Transport Module: in SFEI -> warehouse

                data.add(0, "SFEM_T_WH2IN");
                data.add(1, "SFEE_T_WH2IN");

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

        serializer.getInstance().new_cSFEM_transport(transportConfig, data, controllerData, operationModeData);

    }
}
