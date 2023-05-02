import communication.database.db_part;
import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import controllers.warehouse.cSFEM_warehouse;
import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_warehouse;
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

                cSFEMWarehouse.init("sfee_warehouse");

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
                } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS)) {
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
                } else if (serializer.getInstance().scene.equals(serializer.scenes.WH_SS)) {
                    nModules = 0;
                } else
                    nModules = 1;

                // Special Transport Module -> warehouse
                ArrayList<Object> wh_inData = new ArrayList<>();
                wh_inData.add(0, "SFEM_T_WH2IN");
                wh_inData.add(1, "SFEE_T_WH2IN");

                ArrayList<Object> initController_wh_inData = new ArrayList<>();

                System.out.println("SFEM_T_WH2IN output SFEE? ");
                Pair<SFEE, cSFEM_production> wh_outSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                System.out.println("Connection Output SFEI?");
                int index = 0;
                for (Map.Entry<Integer, SFEI> entry : wh_outSFEE.getFirst().getSFEIs().entrySet()) {
                    System.out.println("  " + index + " -> " + entry.getValue().getName());
                    index++;
                }
                SFEI wh_outSFEI = wh_outSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));
                SFEE_transport viewer = new SFEE_transport();

                String[] wh_out_SensAct = viewer.associateSensor2Actuator(3, wh_outSFEI.getInSensor().getName());

                initController_wh_inData.add(0, "sfei_trans_WH2SS");
                // MB connection
                initController_wh_inData.add(1, null);
                initController_wh_inData.add(2, wh_outSFEE.getSecond().searchMBbySFEE(wh_outSFEE.getFirst().getName()));
                // in/out SFEE
                initController_wh_inData.add(3, cSFEMWarehouse.getSfem().getSfeeWarehouse());
                initController_wh_inData.add(4, wh_outSFEE.getFirst());
                // in/out SFEI
                initController_wh_inData.add(5, cSFEMWarehouse.getSfem().getSfeeWarehouse().getSFEIbyIndex(0));
                initController_wh_inData.add(6, wh_outSFEI.getName());

                initController_wh_inData.add("none");
                initController_wh_inData.addAll(List.of(wh_out_SensAct));

                ArrayList<Object> init_OperationMode_wh_inData = new ArrayList<>(List.of(viewer.SFEE_stochasticTime()));

                wh_inData.addAll(initController_wh_inData);
                wh_inData.addAll(init_OperationMode_wh_inData);

                serializer.getInstance().new_cSFEM_transport(wh_inData, true);

                for (int i = 0; i < nModules; i++) {
/*                    SFEM_transport sfemTransport = new SFEM_transport("SFEM_Trans#" + i);
                    cSFEM_transport sfemController = new cSFEM_transport(sfemTransport);

                    sfemController.initSFEE_transport_FromTerminal();*/
                    System.out.print("SFEM transport name ? ");
                    String sfem_transport_name = in.nextLine();

                    System.out.print("SFEE transport name ? ");
                    String sfee_transport_name = in.nextLine();

                    System.out.println(sfem_transport_name + " input SFEE? ");
                    Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                    System.out.println("Connection Input SFEI?");
                    index = 0;
                    for (Map.Entry<Integer, SFEI> entry : inSFEE.getFirst().getSFEIs().entrySet()) {
                        System.out.println("  " + index + " -> " + entry.getValue().getName());
                        index++;
                    }
                    SFEI inSFEI = inSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));

                    System.out.println(sfem_transport_name + " output SFEE? ");
                    Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(in.nextLine());
                    System.out.println("Connection Output SFEI?");
                    index = 0;
                    for (Map.Entry<Integer, SFEI> entry : outSFEE.getFirst().getSFEIs().entrySet()) {
                        System.out.println("  " + index + " -> " + entry.getValue().getName());
                        index++;
                    }
                    SFEI outSFEI = outSFEE.getFirst().getSFEIbyIndex(Integer.parseInt(in.nextLine()));
//                    SFEE_transport viewer = new SFEE_transport();

                    String[] in_SensAct = viewer.associateSensor2Actuator(1, inSFEI.getOutSensor().getName());
                    String[] out_SensAct = viewer.associateSensor2Actuator(3, outSFEI.getInSensor().getName());

                    ArrayList<Object> initController_data = new ArrayList<>();
                    initController_data.add(0, "SFEI_transport " + i);
                    initController_data.add(1, inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()));
                    initController_data.add(2, outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()));
                    initController_data.add(3, inSFEE.getFirst());
                    initController_data.add(4, outSFEE.getFirst());
                    initController_data.add(5, inSFEI);
                    initController_data.add(6, outSFEI);
                    initController_data.addAll(List.of(in_SensAct));
                    initController_data.addAll(List.of(out_SensAct));


                    ArrayList<Object> init_OperationMode_data = new ArrayList<>(List.of(viewer.SFEE_stochasticTime()));

//                    sfemController.init_cSFEETransport(initController_data, init_OperationMode_data);
//                    serializer.getInstance().getC_Transport().insert(i, sfemController);
                    ArrayList<Object> data = new ArrayList<>();
                    data.add(0, sfem_transport_name);
                    data.add(1, sfee_transport_name);
                    data.addAll(initController_data);
                    data.addAll(init_OperationMode_data);

                    serializer.getInstance().new_cSFEM_transport(data, false);

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

                // Open communications
                for (cSFEM_production production : serializer.getInstance().getC_Production()) {
                    production.init_after_XML_loading();
                }

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

                    if (names.getFirst().getFirst().equalsIgnoreCase("sfee_warehouse")) {

                        Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(names.getSecond().getFirst());
                        transport.setupSFEETransportController(
                                null,
                                outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()),
                                serializer.getInstance().getC_Warehouse().getSfeeWarehouseController().getSfee().getSFEIbyIndex(0),
                                serializer.getInstance().searchSFEIbySFEE(outSFEE.getFirst(), names.getSecond().getSecond()));
                    } else {
                        Pair<SFEE, cSFEM_production> inSFEE = serializer.getInstance().searchSFEEbyName(names.getFirst().getFirst());
                        Pair<SFEE, cSFEM_production> outSFEE = serializer.getInstance().searchSFEEbyName(names.getSecond().getFirst());

                        transport.setupSFEETransportController(
                                inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()),
                                outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()),
                                serializer.getInstance().searchSFEIbySFEE(inSFEE.getFirst(), names.getFirst().getSecond()),
                                serializer.getInstance().searchSFEIbySFEE(outSFEE.getFirst(), names.getSecond().getSecond()));
                    }

                }
//                serializer.getInstance().saveXML();

            }

            // DB
            serializer.getInstance().updateDB();
            // update index of partID in the warehouse
//            System.out.println(db_part.getInstance().getAll_parts(serializer.getInstance().scene.toString()).size());
            serializer.getInstance().getC_Warehouse().updatePartIdOffset(db_part.getInstance().getAll_parts(serializer.getInstance().scene.toString()).size());

            System.out.print("Press ENTER to start simulation");
            in.nextLine();
            // Function for start all simulations
            // 1 SFEM -> having 1SFEE or nSFEEs, is the same because the modbus connection is only 1 per simulation
            for (cSFEM_production production : serializer.getInstance().getC_Production()) {
                production.startSimulation();
            }

//        exit(0);


            int poolsize = serializer.getInstance().getC_Production().size() + serializer.getInstance().getC_Transport().size() + 1;
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolsize);

            scheduler.scheduleAtFixedRate(serializer.getInstance().getC_Warehouse(), 0, 100, TimeUnit.MILLISECONDS);

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


}
