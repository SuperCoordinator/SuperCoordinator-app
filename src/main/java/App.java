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

    private final String filePath = "C:\\Users\\danie\\Desktop\\SFEM_production.ser";

    public void serialize(cSFEM_production obj) {

        try {
            // serialize object's state
            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(obj);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public cSFEM_production deserialize() {

        cSFEM_production obj = null;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            obj = (cSFEM_production) inputStream.readObject();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static void main(String[] args) {


        App app = new App();

/*        SFEM_production newSFEM = new SFEM_production("SFEM_production test");

        cSFEM_production sfem1Controller = new cSFEM_production(newSFEM);
        sfem1Controller.init_SFEEs(2);
        sfem1Controller.init_SFEE_controllers(1);

        app.serialize(sfem1Controller);*/

        cSFEM_production sfem1Controller = app.deserialize();


        sfem1Controller.openConnections();
        SFEM_production newSFEM = sfem1Controller.getSfem();

        SFEM_transport newSFEM_transp = new SFEM_transport("SFEM_transport");
        cSFEM_transport sfemTransportController = new cSFEM_transport(newSFEM_transp);
        sfemTransportController.init_SFEE_transport();
        sfemTransportController.init_SFEE_transport_controller(
                sfem1Controller.searchMBbySFEE(newSFEM.getSFEEs().get(0).getName()),
                sfem1Controller.searchMBbySFEE(newSFEM.getSFEEs().get(1).getName()),
                newSFEM.getSFEEs().get(0),
                newSFEM.getSFEEs().get(1));

        // Do the serialization here, before runtime


        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
            scheduler.scheduleAtFixedRate(sfem1Controller, 0, 100, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(sfemTransportController, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
