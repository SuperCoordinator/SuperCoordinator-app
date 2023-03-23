import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_transport;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class App {

    private final String filePath = "C:\\Users\\danie\\Desktop\\SFEM_production.txt";

    public void serialize(SFEM_production SFEM) {

        try {
            // serialize object's state
            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(SFEM);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public SFEM_production deserialize() {

        SFEM_production sfemProduction = null;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            sfemProduction = (SFEM_production) inputStream.readObject();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sfemProduction;
    }

    public static void main(String[] args) {


        App app = new App();

        SFEM_production newSFEM = new SFEM_production("SFEM_production test");
        app.serialize(newSFEM);

//        SFEM_production newSFEM = app.deserialize();

        cSFEM_production sfemController = new cSFEM_production(newSFEM);
        sfemController.init_SFEEs();
        sfemController.init_SFEE_controllers();


        sfem1Controller.openConnections();
        sfem2Controller.openConnections();

        SFEM_transport newSFEM_transp = new SFEM_transport("SFEM_transport");
        cSFEM_transport sfemTransportController = new cSFEM_transport(newSFEM_transp);
        sfemTransportController.init_SFEE_transport();
        sfemTransportController.init_SFEE_transport_controller(

                sfemController.searchMBbySFEE(newSFEM.getSFEEs().get(0).getName()),
                sfemController.searchMBbySFEE(newSFEM.getSFEEs().get(1).getName()),
                newSFEM.getSFEEs().get(0),
                newSFEM.getSFEEs().get(1));

        // Do the serialization here, before runtime


        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
            scheduler.scheduleAtFixedRate(sfem1Controller, 0, 100, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(sfem2Controller, 0, 100, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(sfemTransportController, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
