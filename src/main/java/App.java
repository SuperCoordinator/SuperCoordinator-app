import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;
import org.apache.commons.math3.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class App {
    enum scenes {
        CMC_connection,
        CMC2_con_individual
    }

    public final scenes scene = scenes.CMC2_con_individual;
    private final String prod_filePath = "blocks/" + scene + "/saves/SFEM_production.ser";
    private final String trans_filePath = "blocks/" + scene + "/saves/SFEM_transport.ser";
    private ArrayList<cSFEM_production> C_Production = new ArrayList<>();
    private ArrayList<cSFEM_transport> C_Transport = new ArrayList<>();

    public ArrayList<cSFEM_production> getC_Production() {
        return C_Production;
    }

    public void setC_Production(ArrayList<cSFEM_production> c_Production) {
        C_Production = c_Production;
    }

    public ArrayList<cSFEM_transport> getC_Transport() {
        return C_Transport;
    }

    public void setC_Transport(ArrayList<cSFEM_transport> c_Transport) {
        C_Transport = c_Transport;
    }


    private void serialize_prod() {

        try {
            // serialize object's state
            FileOutputStream fos = new FileOutputStream(prod_filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(C_Production);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void serialize_trans() {

        try {
            // serialize object's state
            FileOutputStream fos = new FileOutputStream(trans_filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(C_Transport);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deserialize_prod() {

        try {
            FileInputStream fis = new FileInputStream(prod_filePath);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            C_Production = new ArrayList<>((ArrayList<cSFEM_production>) inputStream.readObject());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deserialize_trans() {

        try {
            FileInputStream fis = new FileInputStream(trans_filePath);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            C_Transport = new ArrayList<>((ArrayList<cSFEM_transport>) inputStream.readObject());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private Pair<SFEE, cSFEM_production> searchSFEEbyName(String name) {
        Pair<SFEE, cSFEM_production> sfee = null;
        for (cSFEM_production sfemController : C_Production) {
            for (Map.Entry<Integer, SFEE> currentSFEE : sfemController.getSfem().getSFEEs().entrySet()) {
                if (currentSFEE.getValue().getName().equals(name)) {
                    sfee = new Pair<>(currentSFEE.getValue(), sfemController);
                    break;
                }
            }
        }
        if (sfee == null)
            throw new RuntimeException("SFEE not found");

        return sfee;
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        App app = new App();

        boolean newConfig = false;

        try {
            if (newConfig) {
                // Create new configuration files
                /* PRODUCTION MODULES*/
/*            System.out.println("How many Production SFEModules ?");
            String str = in.nextLine();
            int nModules = Integer.parseInt(str);*/
                int nModules = 1, nSFEE = 1;

                if (app.scene.equals(scenes.CMC_connection)) {
                    nModules = 1;
                    nSFEE = 2;
                } else if (app.scene.equals(scenes.CMC2_con_individual)) {
                    nModules = 2;
                    nSFEE = 1;
                }

                for (int i = 0; i < nModules; i++) {
                    SFEM_production sfemProduction = new SFEM_production("SFEM_Prod#" + i);

                    cSFEM_production sfemController = new cSFEM_production(sfemProduction);
                    sfemController.init_SFEEs(nSFEE);
                    sfemController.init_SFEE_controllers(app.scene.ordinal(), i);

                    app.getC_Production().add(i, sfemController);
                }
                // Serialize Production_Controllers
                app.serialize_prod();

                // Open communications
                for (cSFEM_production production : app.getC_Production()) {
                    production.openConnections();

                }

                /* TRANSPORT MODULES*/
/*            System.out.println("How many Transport SFEModules ?");
            String str = in.nextLine();
            int nModules = Integer.parseInt(str);*/
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

                    app.getC_Transport().add(i, sfemController);
                }

                app.serialize_trans();

            } else {
                // Load existing configuration files
                // Deserialize Production Controllers
                app.deserialize_prod();

                // Open communications
                for (cSFEM_production production : app.getC_Production()) {
                    production.openConnections();

                }

                // Deserialize Transport Controllers
                app.deserialize_trans();

                // Set up the connections between SFEEs
                for (cSFEM_transport transport : app.getC_Transport()) {
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
            for (cSFEM_production production : app.getC_Production()) {
                production.startSimulation();
            }

//        exit(0);


            int poolsize = app.getC_Transport().size() + app.getC_Production().size();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolsize);

            for (cSFEM_production production : app.getC_Production()) {
                scheduler.scheduleAtFixedRate(production, 0, 100, TimeUnit.MILLISECONDS);
            }

            for (cSFEM_transport transport : app.getC_Transport()) {
                scheduler.scheduleAtFixedRate(transport, 0, 100, TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



/*        SFEM_production newSFEM = new SFEM_production("SFEM_production test");

        cSFEM_production sfem1Controller = new cSFEM_production(newSFEM);
        sfem1Controller.init_SFEEs(2);
        sfem1Controller.init_SFEE_controllers(1);

        app.serialize_prod(sfem1Controller);*/

/*        cSFEM_production sfem1Controller = app.deserialize_prod();
        SFEM_production newSFEM = sfem1Controller.getSfem();
        sfem1Controller.openConnections();*/

 /*      SFEM_transport newSFEM_transp = new SFEM_transport("SFEM_transport");
        cSFEM_transport sfemTransportController = new cSFEM_transport(newSFEM_transp);
        sfemTransportController.init_SFEE_transport();
        sfemTransportController.initSFEETransportController(
                sfem1Controller.searchMBbySFEE(newSFEM.getSFEEs().get(0).getName()),
                sfem1Controller.searchMBbySFEE(newSFEM.getSFEEs().get(1).getName()),
                newSFEM.getSFEEs().get(0),
                newSFEM.getSFEEs().get(1));
        app.serialize_trans(sfemTransportController);*/

        // Do the serialization here, before runtime
/*
        cSFEM_transport sfemTransportController = app.deserialize_trans();
        sfemTransportController.setupSFEETransportController(
                sfem1Controller.searchMBbySFEE(newSFEM.getSFEEs().get(0).getName()),
                sfem1Controller.searchMBbySFEE(newSFEM.getSFEEs().get(1).getName()),
                newSFEM.getSFEEs().get(0),
                newSFEM.getSFEEs().get(1));
*/

//        exit(0);

/*        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
            scheduler.scheduleAtFixedRate(sfem1Controller, 0, 100, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(sfemTransportController, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }


}
