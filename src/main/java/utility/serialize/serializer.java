package utility.serialize;

import communication.database.*;
import controllers.production.cSFEE_production;
import controllers.production.cSFEM_production;
import controllers.transport.cSFEM_transport;
import controllers.warehouse.cSFEM_warehouse;
import models.sfe_x.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import org.apache.commons.math3.util.Pair;
import org.apache.ibatis.jdbc.ScriptRunner;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class serializer {

    /**
     * Singleton pattern
     */
    public serializer() {
    }

    public static serializer getInstance() {
        return serializer.serializerHolder.INSTANCE;
    }

    private static class serializerHolder {
        private static final serializer INSTANCE = new serializer();
    }

    //    public enum scenes {
//        CMC_connection,
//        CMC2_con_individual,
//        sorting_station,
//        WH_SS_3CMC,
//        MC_Staudinger,
//        WH_SS_WH,
//        WH_SS_3CMC_WH,
//        WH_SS_3CMC_MCS_WH
//    }
//
//    public final scenes scene = scenes.WH_SS_3CMC_WH;
//    private final String filePath = "blocks/" + scene + "/saves/" + scene;
    public String scene;
    private serializable serializable = new serializable();

    public void setC_Warehouse(cSFEM_warehouse cSFEMWarehouse) {
        serializable.setC_Warehouse(cSFEMWarehouse);
    }

    public cSFEM_warehouse getC_Warehouse() {
        return serializable.getC_Warehouse();
    }

    public ArrayList<cSFEM_production> getC_Production() {
        return serializable.getC_Production();
    }

    public ArrayList<cSFEM_transport> getC_Transport() {
        return serializable.getC_Transport();
    }

    public void saveXML(String filePath) {

        try {
            dbConnection.getInstance().getConnection();
            String fileName = new File(filePath).getName();
            String scene = fileName.split(".xml")[0];
            this.scene = scene;
            createDB(scene);

            JAXBContext context = JAXBContext.newInstance(utility.serialize.serializable.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(serializable, new File(filePath));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    public void loadXML(String filePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(utility.serialize.serializable.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            serializable = (utility.serialize.serializable) unmarshaller.unmarshal(new FileReader(filePath));
            dbConnection.getInstance().getConnection();

            String fileName = new File(filePath).getName();
            String scene = fileName.split(".xml")[0];
            this.scene = scene;
            createDB(scene);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createDB(String scene) {
        try {
            String query = "CREATE DATABASE IF NOT EXISTS " + scene + ";";
            dbConnection.getInstance().getConnection().prepareStatement(query).executeUpdate();
            dbConnection.getInstance().setDatabase(scene);
            // create tables
            ScriptRunner scriptRunner = new ScriptRunner(dbConnection.getInstance().getConnection());
            //Creating a reader object
            Reader reader = new BufferedReader(new FileReader(serializable.getDatabasePath() + "/DDL.sql"));
            scriptRunner.setLogWriter(null); // not print in terminal
            //Running the script
            scriptRunner.runScript(reader);
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void emptyDB(String scene) {
        try {
            // EMPTY tables
            ScriptRunner scriptRunner = new ScriptRunner(dbConnection.getInstance().getConnection());
            //Creating a reader object
            Reader reader = new BufferedReader(new FileReader(serializable.getDatabasePath() + "/DROP.sql"));
            scriptRunner.setLogWriter(null); // not print in terminal
            //Running the script
            scriptRunner.runScript(reader);

            createDB(scene);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void updateDB(String scene) {
        dbConnection.getInstance().getSf_configuration().insert(scene);
        instantiateSFEx(scene);
    }

    private void instantiateSFEx(String scene) {

        // Instantiate IN-Warehouse
        dbConnection.getInstance().getSfems().insert(getC_Warehouse().getSfem().getName(), scene);

        dbConnection.getInstance().getSfees().insert(getC_Warehouse().getSfeeWarehouseController().getSfee().getName(),
                getC_Warehouse().getSfem().getName());

        getC_Warehouse().getSfeeWarehouseController().getSfee().getSFEIs().forEach((key, value) -> {
            dbConnection.getInstance().getSfeis().insert(
                    value.getName(),
                    getC_Warehouse().getSfeeWarehouseController().getSfee().getName());
        });

/*       dbConnection.getInstance().getSfeis().insert(getC_Warehouse().getSfeeWarehouseController().getSfee().getSFEIbyIndex(0).getName(),
                getC_Warehouse().getSfeeWarehouseController().getSfee().getName());*/

        // Invented in sensor -> warehouse_entryDoor
        dbConnection.getInstance().getSensors().insert("warehouse_entryDoor",
                getC_Warehouse().getSfeeWarehouseController().getSfee().getSFEIbyIndex(0).getName(),
                true);

        // Invented out sensor -> warehouse_expeditionDoor
        dbConnection.getInstance().getSensors().insert("warehouse_expeditionDoor",
                getC_Warehouse().getSfeeWarehouseController().getSfee().getSFEIbyIndex(1).getName(),
                false);

        // Instantiate Production Elements (and their sensors)
        getC_Production().forEach(cSFEMProduction -> {
            dbConnection.getInstance().getSfems().insert(cSFEMProduction.getSfem().getName(), scene);

            cSFEMProduction.getSfeeControllers().forEach(cSFEEProduction -> {
                dbConnection.getInstance().getSfees().insert(cSFEEProduction.getSFEE().getName(), cSFEMProduction.getSfem().getName());

                cSFEEProduction.getSFEE().getSFEIs().forEach((key, sfei) -> {
                    dbConnection.getInstance().getSfeis().insert(sfei.getName(), cSFEEProduction.getSFEE().getName());

                    //inSensor
                    if (sfei.getInSensor() != null)
                        dbConnection.getInstance().getSensors().insert(sfei.getInSensor().getName(), sfei.getName(), true);

                    //outSensor
                    if (sfei.getOutSensor() != null)
                        dbConnection.getInstance().getSensors().insert(sfei.getOutSensor().getName(), sfei.getName(), false);
                });
            });
        });
    }

    public void new_cSFEM_transport(SFEM_transport.configuration configuration, ArrayList<Object> names, ArrayList<Object> transportControllers, ArrayList<Object> opMode) {
        try {
            SFEM_transport sfemTransport = new SFEM_transport((String) names.get(0), configuration);
            cSFEM_transport sfemController = new cSFEM_transport(sfemTransport);

            sfemController.init_SFEE_transport((String) names.get(1));

            // Perform searches for SFEIs and SFEEs objects based on the elements name
            Pair<SFEE, SFEI> in, out;

            switch (configuration) {
                case WH2SFEI, WH2RealSFEI -> {
                    in = new Pair<>((SFEE) transportControllers.get(3), (SFEI) transportControllers.get(5));
                    out = searchSFEE_SFEIbySFEI_name((String) transportControllers.get(6));
                }
                case SFEI2WH, RealSFEI2WH -> {
                    in = searchSFEE_SFEIbySFEI_name((String) transportControllers.get(5));
                    out = new Pair<>((SFEE) transportControllers.get(4), (SFEI) transportControllers.get(6));
                }
                default -> {
                    in = searchSFEE_SFEIbySFEI_name((String) transportControllers.get(5));
                    out = searchSFEE_SFEIbySFEI_name((String) transportControllers.get(6));
                }
            }

            transportControllers.set(3, in.getFirst());
            transportControllers.set(4, in.getSecond());
            transportControllers.set(5, out.getFirst());
            transportControllers.set(6, out.getSecond());

            sfemController.init_cSFEETransport(configuration, transportControllers, opMode);

            // Add if not exist or update if exists
            int i;
            for (i = 0; i < serializer.getInstance().getC_Transport().size(); i++) {
                if (serializer.getInstance().getC_Transport().removeIf(next -> next.getSfem().getName().equals(names.get(0)))) {
                    serializer.getInstance().getC_Transport().add(i, sfemController);
                    System.out.println("Updated C_Transport");
                    break;
                }
            }

            if (i == serializer.getInstance().getC_Transport().size() /*&& i > 0*/) {
                serializer.getInstance().getC_Transport().add(/*serializer.getInstance().getC_Transport().size() - 1,*/ sfemController);
                System.out.println("Created new C_Transport");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Pair<SFEE, cSFEM_production> searchSFEEbyName(String name) {

        Pair<SFEE, cSFEM_production> sfee = null;
        for (cSFEM_production sfemController : serializable.getC_Production()) {
            for (Map.Entry<Integer, SFEE> currentSFEE : sfemController.getSfem().getSFEEs().entrySet()) {
                if (currentSFEE.getValue().getName().equals(name)) {
                    sfee = new Pair<>(currentSFEE.getValue(), sfemController);
                    break;
                }
            }
        }
        if (sfee == null)
            throw new RuntimeException("(" + serializer.class + ") SFEE " + name + " not found");

        return sfee;
    }

    public SFEI searchSFEIbySFEE(SFEE sfee, String SFEI_name) {
        SFEI sfei = null;
        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
            if (entry.getValue().getName().equals(SFEI_name))
                sfei = entry.getValue();
        }
        if (sfei == null)
            throw new RuntimeException("(" + serializer.class + ") SFEI with name " + SFEI_name + " not found");

        return sfei;

    }

    public Pair<SFEE, SFEI> searchSFEE_SFEIbySFEI_name(String name) {
        Pair<SFEE, SFEI> returnPair = null;
        for (cSFEM_production cSFEMProduction : serializer.getInstance().getC_Production()) {
            for (cSFEE_production cSFEEProduction : cSFEMProduction.getSfeeControllers()) {
                for (Map.Entry<Integer, SFEI> entry : cSFEEProduction.getSFEE().getSFEIs().entrySet()) {
                    if (entry.getValue().getName().equals(name)) {
                        returnPair = new Pair<>(cSFEEProduction.getSFEE(), entry.getValue());
                    }
                }
            }
        }
        if (returnPair == null)
            throw new RuntimeException("SFEI " + name + " not found in any SFEM");

        return returnPair;

    }

    public void setFailuresHistoryPath(String path) {
        serializable.setFailuresHistoryPath(path);
    }

    public void setInboundOrdersPath(String path) {
        serializable.setInboundOrdersPath(path);
    }

    public void setDatabasePath(String path) {
        serializable.setDatabasePath(path);
    }

    public String getInboundOrdersPath() {
        return serializable.getInboundOrdersPath();
    }

    public String getDatabasePath() {
        return serializable.getDatabasePath();
    }

    public void saveFailuresHistory() {
        try {
            FailureOccurrenceArray failureOccurrenceArray = new FailureOccurrenceArray();

            for (cSFEM_production cSFEMProduction : serializer.getInstance().getC_Production()) {
                for (cSFEE_production cSFEEProduction : cSFEMProduction.getSfeeControllers()) {
                    for (SFEI sfei : cSFEEProduction.getSFEE().getSFEIs().values()) {
                        failureOccurrenceArray.getFailuresOccurrences().addAll(sfei.getFailuresHistory().values());
                    }
                }
            }

            File f = new File(serializable.getFailuresHistoryPath());

            JAXBContext context = JAXBContext.newInstance(FailureOccurrenceArray.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(failureOccurrenceArray, new File(serializable.getFailuresHistoryPath() + "/failuresOccurrences" + Objects.requireNonNull(f.list()).length + ".xml"));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
