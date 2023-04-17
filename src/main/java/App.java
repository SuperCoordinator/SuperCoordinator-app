import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;
import org.apache.commons.math3.util.Pair;
import utils.serialize.serializer;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;


public class App {


    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        serializer app = new serializer();

        boolean newConfig = false;

        try {
            if (newConfig) {
                // Create new configuration files
                /* PRODUCTION MODULES*/
                int nModules = 1, nSFEE = 1;

                if (app.scene.equals(serializer.scenes.CMC_connection)) {
                    nModules = 1;
                    nSFEE = 2;
                } else if (app.scene.equals(serializer.scenes.CMC2_con_individual)) {
                    nModules = 2;
                    nSFEE = 1;
                } else if (app.scene.equals(serializer.scenes.sorting_station)) {
                    nModules = 1;
                    nSFEE = 3;
                } else if (app.scene.equals(serializer.scenes.SS_3CMC)) {
                    nModules = 2;
                    nSFEE = 3;
                }

                for (int i = 0; i < nModules; i++) {
                    SFEM_production sfemProduction = new SFEM_production("SFEM_Prod#" + i);

                    cSFEM_production sfemController = new cSFEM_production(sfemProduction);
                    sfemController.init_SFEEs(nSFEE);
                    sfemController.init_SFEE_controllers(app.scene.ordinal(), i);

                    app.getProduction().getC_Production().add(i, sfemController);
                }
                // Serialize Production_Controllers
                app.saveXML_prod();

                // Open communications
                for (cSFEM_production production : app.getProduction().getC_Production()) {
                    production.openConnections();

                }

                /* TRANSPORT MODULES*/
/*            System.out.println("How many Transport SFEModules ?");
            String str = in.nextLine();
            int nModules = Integer.parseInt(str);*/
                if (app.scene.equals(serializer.scenes.sorting_station))
                    nModules = 0;
                else if(app.scene.equals(serializer.scenes.SS_3CMC)){
                    nModules = 3;
                }
                else
                    nModules = 1;

                for (int i = 0; i < nModules; i++) {

                    SFEM_transport sfemTransport = new SFEM_transport("SFEM_Trans#" + i);
                    cSFEM_transport sfemController = new cSFEM_transport(sfemTransport);
                    sfemController.init_SFEE_transport();

                    System.out.println(sfemTransport.getName() + " input SFEE? ");
                    Pair<SFEE, cSFEM_production> inSFEE = app.searchSFEEbyName(in.nextLine());
                    System.out.println(sfemTransport.getName() + " output SFEE? ");
                    Pair<SFEE, cSFEM_production> outSFEE = app.searchSFEEbyName(in.nextLine());

                    sfemController.initSFEETransportController(
                            inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()),
                            outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()),
                            inSFEE.getFirst(),
                            outSFEE.getFirst());

                    app.getTransport().getC_Transport().add(i, sfemController);
                }

                app.saveXML_trans();

//                exit(0);

            } else {
                // Load existing configuration files
                // Deserialize Production Controllers

                app.loadXML_prod();
                // Open communications
                for (cSFEM_production production : app.getProduction().getC_Production()) {
                    production.init_after_XML_loading();
                }

                for (cSFEM_production production : app.getProduction().getC_Production()) {
                    production.openConnections();

                }

                // Deserialize Transport Controllers
                app.loadXML_trans();
                // Initialize the monitors and controllers with the SFEE objects
                for (cSFEM_transport transport : app.getTransport().getC_Transport()) {
                    transport.init_after_XML_load();
                }

                // Set up the connections between SFEEs
                for (cSFEM_transport transport : app.getTransport().getC_Transport()) {
                    Pair<String, String> names = transport.getPrevNextSFEE_names();

                    Pair<SFEE, cSFEM_production> inSFEE = app.searchSFEEbyName(names.getFirst());
                    Pair<SFEE, cSFEM_production> outSFEE = app.searchSFEEbyName(names.getSecond());

                    transport.setupSFEETransportController(
                            inSFEE.getSecond().searchMBbySFEE(inSFEE.getFirst().getName()),
                            outSFEE.getSecond().searchMBbySFEE(outSFEE.getFirst().getName()),
                            inSFEE.getFirst(),
                            outSFEE.getFirst());

                }

            }

            System.out.print("Press ENTER to start simulation");
            in.nextLine();
            // Function for start all simulations
            // 1 SFEM -> having 1SFEE or nSFEEs, is the same because the modbus connection is only 1 per simulation
            for (cSFEM_production production : app.getProduction().getC_Production()) {
                production.startSimulation();
            }

//        exit(0);


            int poolsize = app.getProduction().getC_Production().size() + app.getTransport().getC_Transport().size();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolsize);

            for (cSFEM_production production : app.getProduction().getC_Production()) {
                scheduler.scheduleAtFixedRate(production, 0, 100, TimeUnit.MILLISECONDS);
            }

            for (cSFEM_transport transport : app.getTransport().getC_Transport()) {
                scheduler.scheduleAtFixedRate(transport, 0, 100, TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
